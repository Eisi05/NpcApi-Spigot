package de.eisi05.npc.api.wrapper;

import com.google.common.primitives.Primitives;
import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.utils.CallerUtils;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.utils.exceptions.VersionNotFound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Wrapper implements HandleHolder
{
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Constructor<?>> wrapperConstructorCache = new ConcurrentHashMap<>();
    private static final Map<String, Constructor<?>> instanceConstructorCache = new ConcurrentHashMap<>();
    private static final Map<String, MethodHandle> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, MethodHandle> fieldGetterCache = new ConcurrentHashMap<>();
    private static final Map<String, MethodHandle> fieldSetterCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> enumFieldCache = new ConcurrentHashMap<>();

    protected final Object handle;

    public Wrapper(Object handle)
    {
        this.handle = handle;
    }

    public static Object[] checkArgs(Object... args)
    {
        return Arrays.stream(args)
                .map(arg -> (arg instanceof HandleHolder h) ? h.getHandle() : arg)
                .toArray();
    }

    public static @Nullable Class<?> getWrappedClass(@NotNull Class<?> wrapperClass)
    {
        Mapping[] annotations = wrapperClass.getAnnotationsByType(Mapping.class);

        for(Mapping mapping : annotations)
        {
            if(!Versions.containsCurrentVersion(mapping))
                continue;

            return getTargetClass(mapping);
        }

        throw new VersionNotFound(wrapperClass);
    }

    protected static Class<?> getTargetClass(Mapping mapping)
    {
        return classCache.computeIfAbsent(mapping.path(), name ->
        {
            try
            {
                return Class.forName(name);
            } catch(ClassNotFoundException e)
            {
                return null;
            }
        });
    }

    protected static Constructor<?> findConstructor(Class<?> targetClass, Object[] args)
    {
        return Arrays.stream(targetClass.getDeclaredConstructors())
                .filter(ctor ->
                {
                    Class<?>[] params = ctor.getParameterTypes();
                    boolean varArgs = ctor.isVarArgs();

                    if((!varArgs && params.length != args.length) ||
                            (varArgs && args.length < params.length - 1))
                        return false;

                    for(int i = 0; i < params.length; i++)
                    {
                        if(i >= args.length)
                            return false;

                        Object arg = args[i];
                        if(arg == null)
                            continue;

                        Class<?> actual = arg.getClass();
                        if(actual.isAnonymousClass() || actual.isSynthetic())
                        {
                            Class<?>[] interfaces = actual.getInterfaces();
                            actual = interfaces.length > 0 ? interfaces[0] : actual.getSuperclass();
                        }

                        if(varArgs && i == params.length - 1)
                        {
                            Class<?> comp = params[i].getComponentType();
                            if(!Primitives.wrap(comp).isAssignableFrom(Primitives.wrap(actual)) && !actual.isArray())
                                return false;
                        }
                        else
                        {
                            if(!Primitives.wrap(params[i]).isAssignableFrom(Primitives.wrap(actual)))
                                return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    protected static Object createInstance(Class<? extends Wrapper> wrapperClass, Object... args)
    {
        Object[] checkedArgs = checkArgs(args);
        try
        {
            Mapping[] annotations = wrapperClass.getAnnotationsByType(Mapping.class);

            for(Mapping mapping : annotations)
            {
                if(!Versions.containsCurrentVersion(mapping))
                    continue;

                Class<?> targetClass = getTargetClass(mapping);

                String cacheKey = targetClass.getName() +
                        Arrays.toString(Arrays.stream(checkedArgs)
                                .map(a -> a == null ? "null" : a.getClass().getName())
                                .toArray());

                Constructor<?> constructor = instanceConstructorCache.computeIfAbsent(cacheKey, k -> findConstructor(targetClass, checkedArgs));

                if(constructor == null)
                    throw new NoSuchMethodException("No matching constructor found for: " + targetClass.getName() + "(" +
                            Arrays.toString(checkedArgs) + ")");

                Object[] realArgs;

                if(constructor.isVarArgs())
                {
                    int fixedCount = constructor.getParameterCount() - 1;
                    Class<?> varArgType = constructor.getParameterTypes()[fixedCount].getComponentType();
                    Object varArgsArray = Array.newInstance(varArgType, args.length - fixedCount);

                    for(int i = 0; i < Array.getLength(varArgsArray); i++)
                        Array.set(varArgsArray, i, checkedArgs[fixedCount + i]);

                    realArgs = new Object[constructor.getParameterCount()];
                    System.arraycopy(checkedArgs, 0, realArgs, 0, fixedCount);
                    realArgs[fixedCount] = varArgsArray;
                }
                else
                    realArgs = checkedArgs;

                constructor.setAccessible(true);
                return constructor.newInstance(realArgs);
            }

            throw new VersionNotFound(wrapperClass);
        } catch(Exception e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            return null; //throw new RuntimeException(e);
        }
    }

    protected static <T extends Wrapper> T createWrappedInstance(Class<T> wrapperClass, Object... args)
    {
        try
        {
            Constructor<?> cached = wrapperConstructorCache.computeIfAbsent(wrapperClass, clazz ->
            {
                try
                {
                    Constructor<?> cons = clazz.getDeclaredConstructor(Object.class);
                    cons.setAccessible(true);
                    return cons;
                } catch(Exception e)
                {
                    if(NpcApi.config.debug())
                        e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });

            return wrapperClass.cast(cached.newInstance(createInstance(wrapperClass, args)));
        } catch(Exception e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            return null; //throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getStaticWrappedFieldValue(String fieldName)
    {
        try
        {
            Class<?> callerClass = CallerUtils.getCallerClass();
            Field callerField = callerClass.getDeclaredField(fieldName);

            Mapping[] fieldAnnotations = callerField.getAnnotationsByType(Mapping.class);

            Mapping[] classAnnotations = callerClass.getAnnotationsByType(Mapping.class);
            if(classAnnotations.length == 0)
                throw new IllegalStateException("Missing @WrapData on class: " + callerClass.getName());

            Mapping fieldAnnotation = Arrays.stream(fieldAnnotations).filter(Versions::containsCurrentVersion).findFirst()
                    .orElseThrow(() -> new VersionNotFound(callerClass.getName() + " -> " + callerField.getName()));

            for(Mapping classData : classAnnotations)
            {
                if(!Versions.containsCurrentVersion(classData))
                    continue;

                String key = classData.path() + "#" + fieldAnnotation.path();

                Field targetField = fieldCache.computeIfAbsent(key, k ->
                {
                    try
                    {
                        Class<?> targetClass = getTargetClass(classData);
                        Field f = targetClass.getDeclaredField(fieldAnnotation.path());
                        f.setAccessible(true);
                        return f;
                    } catch(Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                });

                return Optional.ofNullable((T) targetField.get(null));
            }

            return Optional.empty();
        } catch(Exception e)
        {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeStaticWrappedMethod(Object... args)
    {
        final Object[] newArgs = checkArgs(args);
        try
        {
            String callingMethodName = CallerUtils.getCallerMethodName();
            Class<?> callerClass = CallerUtils.getCallerClass();

            Class<?> targetClass = getTargetClass(callerClass.getAnnotation(Mapping.class));

            Method callingMethod = null;
            Class<?> clazz = callerClass;
            while(callingMethod == null && clazz != null)
            {
                callingMethod = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals(callingMethodName))
                        .findFirst()
                        .orElse(null);
                clazz = clazz.getSuperclass();
            }

            if(callingMethod == null)
                throw new NoSuchElementException("No value present -> " + callingMethodName);

            Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

            MethodHandles.Lookup lookup = CallerUtils.getLookup(targetClass);

            for(Mapping mapping : annotations)
            {
                if(!Versions.containsCurrentVersion(mapping))
                    continue;

                String methodPath = mapping.path();
                String key = targetClass.getName() + "#" + methodPath +
                        Arrays.toString(Arrays.stream(newArgs)
                                .map(a -> a == null ? "null" : a.getClass().getName()).toArray());

                MethodHandle handle = methodCache.computeIfAbsent(key, k ->
                {
                    try
                    {
                        Method method = Arrays.stream(targetClass.getMethods())
                                .filter(m -> m.getName().equals(methodPath))
                                .filter(m ->
                                {
                                    Class<?>[] params = m.getParameterTypes();
                                    if(params.length != newArgs.length)
                                        return false;
                                    for(int i = 0; i < params.length; i++)
                                    {
                                        if(newArgs[i] == null)
                                            continue;
                                        if(!Primitives.wrap(params[i]).isAssignableFrom(Primitives.wrap(newArgs[i].getClass())))
                                            return false;
                                    }
                                    return true;
                                })
                                .findFirst()
                                .orElseThrow(() -> new NoSuchMethodException(methodPath));

                        method.setAccessible(true);
                        return lookup.unreflect(method);
                    } catch(Exception e)
                    {
                        if(NpcApi.config.debug())
                            e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

                return (T) handle.invokeWithArguments(newArgs);
            }

            throw new VersionNotFound(callingMethod);
        } catch(Throwable e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            return null; //throw new RuntimeException(e);
        }
    }

    protected String getPath()
    {
        String callingMethodName = CallerUtils.getCallerMethodName();

        Method callingMethod = Arrays.stream(getClass().getMethods())
                .filter(m -> m.getName().equals(callingMethodName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(callingMethodName + " -> " + getClass()));

        Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

        return Arrays.stream(annotations).filter(Versions::containsCurrentVersion).findFirst()
                .orElseThrow(() -> new VersionNotFound(callingMethod)).path();
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeWrappedMethod(Object... args)
    {
        final Object[] newArgs = checkArgs(args);
        try
        {
            String callingMethodName = CallerUtils.getCallerMethodName();

            Method callingMethod = null;
            Class<?> clazz = getClass();
            while(callingMethod == null && clazz != null)
            {
                callingMethod = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals(callingMethodName))
                        .findFirst()
                        .orElse(null);
                clazz = clazz.getSuperclass();
            }

            if(callingMethod == null)
                throw new NoSuchElementException("No value present -> " + callingMethodName);

            Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

            MethodHandles.Lookup lookup = CallerUtils.getLookup(getHandle().getClass());

            for(Mapping mapping : annotations)
            {
                if(!Versions.containsCurrentVersion(mapping))
                    continue;

                String methodPath = mapping.path();
                String key = getHandle().getClass().getName() + "#" + methodPath +
                        Arrays.toString(Arrays.stream(newArgs).map(a -> a == null ? "null" : a.getClass().getName()).toArray());

                MethodHandle unboundHandle = methodCache.computeIfAbsent(key, k ->
                {
                    try
                    {
                        Method method = Arrays.stream(getHandle().getClass().getMethods())
                                .filter(m -> m.getName().equals(methodPath))
                                .filter(m ->
                                {
                                    Class<?>[] params = m.getParameterTypes();
                                    if(params.length != newArgs.length)
                                        return false;

                                    for(int i = 0; i < params.length; i++)
                                    {
                                        if(newArgs[i] == null)
                                            continue;

                                        if(!Primitives.wrap(params[i]).isAssignableFrom(Primitives.wrap(newArgs[i].getClass())))
                                            return false;
                                    }
                                    return true;
                                })
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(new NoSuchMethodException(getHandle().getClass() + " -> " +
                                        methodPath + "(" + Arrays.stream(newArgs).map(o -> o.getClass().toString()).toList() + ")")));

                        method.setAccessible(true);
                        return lookup.unreflect(method);
                    } catch(Exception e)
                    {
                        if(NpcApi.config.debug())
                            e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

                MethodHandle boundHandle = unboundHandle.bindTo(getHandle());
                return (T) boundHandle.invokeWithArguments(newArgs);
            }

            throw new VersionNotFound(callingMethod);
        } catch(Throwable e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getWrappedFieldValue()
    {
        try
        {
            String callingMethodName = CallerUtils.getCallerMethodName();

            Method callingMethod = null;
            Class<?> clazz = getClass();
            while(callingMethod == null && clazz != null)
            {
                callingMethod = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals(callingMethodName))
                        .findFirst()
                        .orElse(null);
                clazz = clazz.getSuperclass();
            }

            if(callingMethod == null)
                throw new NoSuchElementException("No value present -> " + callingMethodName);

            Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

            for(Mapping mapping : annotations)
            {
                if(!Versions.containsCurrentVersion(mapping))
                    continue;

                String fieldName = mapping.path();
                String key = getHandle().getClass().getName() + "#" + fieldName;

                MethodHandle getter = fieldGetterCache.computeIfAbsent(key, k ->
                {
                    try
                    {
                        Field f = null;
                        Class<?> c = getHandle().getClass();

                        while(c != null)
                        {
                            try
                            {
                                f = c.getDeclaredField(fieldName);
                                if(!Modifier.isStatic(f.getModifiers()))
                                    break;
                                f = null;
                            } catch(NoSuchFieldException ignored) {}
                            c = c.getSuperclass();
                        }

                        if(f == null)
                            throw new NoSuchFieldException(fieldName);

                        f.setAccessible(true);

                        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(f.getDeclaringClass(), MethodHandles.lookup());
                        return lookup.findGetter(f.getDeclaringClass(), fieldName, f.getType());
                    } catch(NoSuchFieldException | IllegalAccessException e)
                    {
                        if(NpcApi.config.debug())
                            e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

                return (T) getter.invoke(getHandle());
            }

            throw new VersionNotFound(callingMethod);
        } catch(Throwable e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            return null; //throw new RuntimeException(e);
        }
    }

    protected <T> void setWrappedFieldValue(T value)
    {
        try
        {
            String callingMethodName = CallerUtils.getCallerMethodName();

            Method callingMethod = null;
            Class<?> clazz = getClass();
            while(callingMethod == null && clazz != null)
            {
                callingMethod = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals(callingMethodName))
                        .findFirst()
                        .orElse(null);
                clazz = clazz.getSuperclass();
            }

            if(callingMethod == null)
                throw new NoSuchElementException("No value present -> " + callingMethodName);

            Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

            for(Mapping mapping : annotations)
            {
                if(!Versions.containsCurrentVersion(mapping))
                    continue;

                String fieldName = mapping.path();
                String key = getHandle().getClass().getName() + "#" + fieldName;

                MethodHandle setter = fieldSetterCache.computeIfAbsent(key, k ->
                {
                    try
                    {
                        Field f = null;
                        Class<?> c = getHandle().getClass();

                        while(c != null)
                        {
                            try
                            {
                                f = c.getDeclaredField(fieldName);

                                if(!Modifier.isStatic(f.getModifiers()))
                                    break;
                                f = null;
                            } catch(NoSuchFieldException ignored) {}
                            c = c.getSuperclass();
                        }

                        if(f == null)
                            throw new NoSuchFieldException(fieldName);
                        f.setAccessible(true);

                        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(f.getDeclaringClass(), MethodHandles.lookup());
                        return lookup.findSetter(getHandle().getClass(), fieldName, f.getType());
                    } catch(NoSuchFieldException | IllegalAccessException e)
                    {
                        if(NpcApi.config.debug())
                            e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

                setter.invoke(getHandle(), value);
                return;
            }

            throw new VersionNotFound(callingMethod);
        } catch(Throwable e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();

            //throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Object getHandle()
    {
        return handle;
    }

    public interface EnumWrapper extends HandleHolder
    {
        @SuppressWarnings("unchecked")
        default <T extends Enum<T> & EnumWrapper, V extends Enum<?>> V cast(T object)
        {
            Class<?> enumClass = object.getClass();

            Mapping[] classData = enumClass.getAnnotationsByType(Mapping.class);
            String classPath = Arrays.stream(classData)
                    .filter(Versions::containsCurrentVersion)
                    .map(Mapping::path)
                    .findFirst()
                    .orElseThrow(() -> new VersionNotFound(enumClass));

            Class<?> targetEnum;
            try
            {
                targetEnum = Class.forName(classPath);
            } catch(ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }

            Map<String, Field> fields = enumFieldCache.computeIfAbsent(enumClass, cls ->
            {
                Map<String, Field> map = new ConcurrentHashMap<>();
                for(Field f : cls.getDeclaredFields())
                {
                    for(Mapping ann : f.getAnnotationsByType(Mapping.class))
                    {
                        if(!Versions.containsCurrentVersion(ann))
                            continue;

                        f.setAccessible(true);
                        map.put(f.getName(), f);
                    }
                }
                return map;
            });

            try
            {
                Field sourceField = fields.get(object.name());
                if(sourceField == null)
                    throw new VersionNotFound(enumClass);

                Mapping[] fieldData = sourceField.getAnnotationsByType(Mapping.class);
                String fieldName = Arrays.stream(fieldData)
                        .filter(Versions::containsCurrentVersion)
                        .map(Mapping::path)
                        .findFirst()
                        .orElseThrow(() -> new VersionNotFound(enumClass));

                Field targetField = targetEnum.getDeclaredField(fieldName);
                targetField.setAccessible(true);
                return (V) targetField.get(null);
            } catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        default String getPath()
        {
            String callingMethodName = CallerUtils.getCallerMethodName();

            Method callingMethod = Arrays.stream(getClass().getMethods())
                    .filter(m -> m.getName().equals(callingMethodName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(callingMethodName + " -> " + getClass()));

            Mapping[] annotations = callingMethod.getAnnotationsByType(Mapping.class);

            return Arrays.stream(annotations).filter(Versions::containsCurrentVersion).findFirst()
                    .orElseThrow(() -> new VersionNotFound(callingMethod)).path();
        }
    }
}
