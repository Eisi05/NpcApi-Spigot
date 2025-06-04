package de.eisi05.npc.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link CallerUtils} class provides utility methods for retrieving information
 * about the calling class and method in the current stack trace.
 * It leverages {@link StackWalker} for efficient stack traversal and
 * caches {@link MethodHandles.Lookup} instances for performance.
 */
public class CallerUtils
{
    private static final Map<Class<?>, MethodHandles.Lookup> lookupCache = new ConcurrentHashMap<>();
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Retrieves the {@link Class} of the immediate caller of the method that invokes this utility method.
     * It skips the current method and the direct caller of this utility method to find the actual caller.
     *
     * @return The {@link Class} object of the caller, or {@code null} if it cannot be determined.
     */
    public static @Nullable Class<?> getCallerClass()
    {
        return WALKER.walk(frames -> frames.skip(2).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
    }

    /**
     * Retrieves the name of the immediate caller method of the method that invokes this utility method.
     * It skips the current method and the direct caller of this utility method to find the actual caller's method name.
     *
     * @return The name of the caller method as a {@link String}, or {@code null} if it cannot be determined.
     */
    public static @Nullable String getCallerMethodName()
    {
        return WALKER.walk(frames -> frames.skip(2).findFirst().map(StackWalker.StackFrame::getMethodName).orElse(null));
    }

    /**
     * Retrieves a {@link MethodHandles.Lookup} instance for the given class.
     * This method uses a cache to return existing lookup instances for already processed classes,
     * or creates and caches a new one if it's the first time the class is requested.
     *
     * @param clazz The {@link Class} for which to get the {@link MethodHandles.Lookup} instance. Must not be {@code null}.
     * @return A {@link MethodHandles.Lookup} instance associated with the provided class.
     */
    public static @NotNull MethodHandles.Lookup getLookup(@NotNull Class<?> clazz)
    {
        return lookupCache.computeIfAbsent(clazz, c -> MethodHandles.lookup());
    }
}
