package de.eisi05.npc.api.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.interfaces.NpcClickAction;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class ObjectSaver
{
    private static final TypeAdapter<ItemStack> ITEM_STACK_ADAPTER = new TypeAdapter<ItemStack>()
    {
        @Override
        public void write(JsonWriter out, ItemStack value) throws IOException
        {
            if(value == null)
            {
                out.nullValue();
                return;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.set("item", value);

            Map<String, Object> valuesMap = config.getConfigurationSection("item").getValues(true);
            JsonElement element = CLEAN_GSON.toJsonTree(valuesMap);
            CLEAN_GSON.toJson(element, out);
        }

        @Override
        public ItemStack read(JsonReader in) throws IOException
        {
            if(in.peek() == JsonToken.NULL)
            {
                in.nextNull();
                return null;
            }

            JsonElement element = JsonParser.parseReader(in);
            YamlConfiguration config = new YamlConfiguration();

            if (element.isJsonPrimitive())
            {
                String yamlStr = element.getAsString();
                try
                {
                    config.loadFromString(yamlStr);
                    return config.getItemStack("item");
                }
                catch(Exception e)
                {
                    throw new IOException("Failed to deserialize legacy ItemStack string format", e);
                }
            }

            if (!element.isJsonObject()) return null;

            try
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> valuesMap = CLEAN_GSON.fromJson(element, HashMap.class);
                config.createSection("item", valuesMap);
                return config.getItemStack("item");
            }
            catch(Exception e)
            {
                throw new IOException("Failed to reconstruct ItemStack from structured JSON data", e);
            }
        }
    }.nullSafe();

    private static final TypeAdapter<Instant> INSTANT_ADAPTER = new TypeAdapter<Instant>()
    {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException
        {
            if(value == null)
                out.nullValue();
            else
                out.value(value.toString());
        }

        @Override
        public Instant read(JsonReader in) throws IOException
        {
            if(in.peek() == JsonToken.NULL)
            {
                in.nextNull();
                return null;
            }
            return Instant.parse(in.nextString());
        }
    }.nullSafe();

    private static final Gson CLEAN_GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, INSTANT_ADAPTER)
            .registerTypeHierarchyAdapter(ItemStack.class, ITEM_STACK_ADAPTER)
            .create();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, INSTANT_ADAPTER)
            .registerTypeHierarchyAdapter(ItemStack.class, ITEM_STACK_ADAPTER)
            .registerTypeHierarchyAdapter(Goal.class, (JsonDeserializer<Goal>) (json, typeOfT, context) ->
            {
                JsonObject jsonObject = json.getAsJsonObject();
                if(jsonObject.entrySet().isEmpty())
                    return null;
                Map.Entry<String, JsonElement> entry = jsonObject.entrySet().iterator().next();
                String goalKey = entry.getKey();
                JsonObject goalData = entry.getValue().getAsJsonObject();
                try
                {
                    String className = goalKey.substring(0, 1).toUpperCase() + goalKey.substring(1) + "Goal";
                    Class<?> clazz = Class.forName("de.eisi05.npc.api.ai." + className);
                    return (Goal) CLEAN_GSON.fromJson(goalData, clazz);
                }
                catch(ClassNotFoundException e)
                {
                    try
                    {
                        return (Goal) CLEAN_GSON.fromJson(goalData, Class.forName(goalKey));
                    }
                    catch(ClassNotFoundException ex)
                    {
                        throw new JsonParseException("Unknown goal: " + goalKey, ex);
                    }
                }
            })
            .registerTypeHierarchyAdapter(Goal.class, (JsonSerializer<Goal>) (src, typeOfSrc, context) ->
            {
                JsonObject wrapper = new JsonObject();
                String shortName = src.getClass().getSimpleName().replace("Goal", "");
                String jsonKey = shortName.isEmpty() ? src.getClass().getName() : shortName.substring(0, 1).toLowerCase() + shortName.substring(1);
                wrapper.add(jsonKey, CLEAN_GSON.toJsonTree(src, src.getClass()));
                return wrapper;
            })
            .registerTypeAdapterFactory(new TypeAdapterFactory()
            {
                @Override
                @SuppressWarnings("unchecked")
                public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
                {
                    Class<?> rawType = typeToken.getRawType();
                    if(rawType != Serializable.class && rawType != NpcClickAction.class)
                        return null;

                    return (TypeAdapter<T>) new TypeAdapter<Object>()
                    {
                        @Override
                        public void write(JsonWriter out, Object value) throws IOException
                        {
                            if(value == null)
                            {
                                out.nullValue();
                                return;
                            }

                            String className = value.getClass().getName();

                            if(className.contains("$$Lambda") || value.getClass().isAnonymousClass() || className.matches(".+\\$\\d+$"))
                            {
                                try
                                {
                                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                    try(ObjectOutputStream objectStream = new ObjectOutputStream(byteStream))
                                    {
                                        objectStream.writeObject(value);
                                    }
                                    String base64Bytes = Base64.getEncoder().encodeToString(byteStream.toByteArray());

                                    JsonObject lambdaWrapper = new JsonObject();
                                    lambdaWrapper.addProperty("type", "DYNAMIC_LAMBDA_FALLBACK");
                                    lambdaWrapper.addProperty("bytes", base64Bytes);
                                    gson.toJson(lambdaWrapper, out);
                                    return;
                                }
                                catch(IOException e)
                                {
                                    out.nullValue();
                                    return;
                                }
                            }

                            JsonElement rawElement = (value instanceof Goal) ? gson.toJsonTree(value, value.getClass()) :
                                    CLEAN_GSON.toJsonTree(value, value.getClass());

                            if(rawElement.isJsonObject())
                            {
                                JsonObject baseObj = rawElement.getAsJsonObject();
                                JsonObject flattened = new JsonObject();
                                flattened.addProperty("type", className);
                                for(Map.Entry<String, JsonElement> entry : baseObj.entrySet())
                                    flattened.add(entry.getKey(), entry.getValue());
                                gson.toJson(flattened, out);
                            }
                            else
                            {
                                JsonObject wrapper = new JsonObject();
                                wrapper.addProperty("type", className);
                                wrapper.add("value", rawElement);
                                gson.toJson(wrapper, out);
                            }
                        }

                        @Override
                        public Object read(JsonReader in)
                        {
                            JsonElement json = JsonParser.parseReader(in);
                            if(json.isJsonPrimitive())
                            {
                                JsonPrimitive prim = json.getAsJsonPrimitive();
                                if(prim.isBoolean())
                                    return prim.getAsBoolean();
                                if(prim.isNumber())
                                    return prim.getAsNumber();
                                return prim.getAsString();
                            }

                            if(!json.isJsonObject())
                                return null;
                            JsonObject obj = json.getAsJsonObject();

                            if(obj.has("type"))
                            {
                                String typeStr = obj.get("type").getAsString();
                                if(typeStr.equals("DYNAMIC_LAMBDA_FALLBACK") && obj.has("bytes"))
                                {
                                    try
                                    {
                                        byte[] data = Base64.getDecoder().decode(obj.get("bytes").getAsString());
                                        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                                        try(ObjectInputStream objectStream = new ObjectInputStream(byteStream))
                                        {
                                            return objectStream.readObject();
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        return null;
                                    }
                                }

                                if(typeStr.contains("$$Lambda") || typeStr.matches(".+\\$\\d+$"))
                                    return null;

                                if(typeStr.contains("."))
                                {
                                    try
                                    {
                                        Class<?> clazz = Class.forName(typeStr);
                                        JsonElement payload = obj.has("value") ? obj.get("value") : obj;
                                        if(payload.isJsonObject())
                                            payload.getAsJsonObject().remove("type");

                                        if(Goal.class.isAssignableFrom(clazz))
                                            return gson.fromJson(payload, clazz);
                                        return CLEAN_GSON.fromJson(payload, clazz);
                                    }
                                    catch(ClassNotFoundException e)
                                    {
                                        throw new JsonIOException(e);
                                    }
                                }
                            }

                            return gson.fromJson(json, HashMap.class);
                        }
                    };
                }
            })
            .registerTypeAdapter(new TypeToken<HashMap<UUID, HashMap<String, Serializable>>>() {}.getType(),
                    (JsonDeserializer<HashMap<UUID, HashMap<String, Serializable>>>) (json, typeOfT, context) ->
                    {
                        HashMap<UUID, HashMap<String, Serializable>> map = new HashMap<>();
                        JsonObject obj = json.getAsJsonObject();

                        for(Map.Entry<String, JsonElement> entry : obj.entrySet())
                        {
                            UUID key;
                            if(entry.getKey().equalsIgnoreCase("global"))
                                key = new UUID(0L, 0L);
                            else
                                key = UUID.fromString(entry.getKey());

                            Type valType = new TypeToken<HashMap<String, Serializable>>() {}.getType();
                            HashMap<String, Serializable> val = context.deserialize(entry.getValue(), valType);
                            map.put(key, val);
                        }
                        return map;
                    })
            .registerTypeAdapter(new TypeToken<HashMap<UUID, HashMap<String, Serializable>>>() {}.getType(),
                    (JsonSerializer<HashMap<UUID, HashMap<String, Serializable>>>) (src, typeOfSrc, context) ->
                    {
                        JsonObject obj = new JsonObject();
                        UUID globalUuid = new UUID(0L, 0L);

                        src.forEach((key, value) ->
                        {
                            String keyStr = key.equals(globalUuid) ? "global" : key.toString();
                            JsonElement valElement = context.serialize(value);
                            obj.add(keyStr, valElement);
                        });
                        return obj;
                    })
            .setPrettyPrinting()
            .create();

    private final File file;

    public ObjectSaver(@NotNull String file)
    {
        this(new File(file));
    }

    public ObjectSaver(@NotNull File file)
    {
        if(file.getParentFile() != null)
        {
            file.getParentFile().mkdirs();
        }
        this.file = file;
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isJson()
    {
        if(!file.exists() || file.length() == 0)
            return true;
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            int firstChar = reader.read();
            while(firstChar != -1 && Character.isWhitespace(firstChar))
            {
                firstChar = reader.read();
            }
            return firstChar == '{' || firstChar == '[';
        }
        catch(IOException e)
        {
            return false;
        }
    }

    public <T> void write(T object) throws IOException
    {
        this.write(object, false);
    }

    public <T> void write(T object, boolean append) throws IOException
    {
        try(Writer writer = new OutputStreamWriter(new FileOutputStream(this.file, append), StandardCharsets.UTF_8))
        {
            GSON.toJson(object, writer);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T read(Class<T> clazz)
    {
        try
        {
            if(!isJson())
            {
                try(FileInputStream fileIn = new FileInputStream(this.file);
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn))
                {
                    return (T) objectIn.readObject();
                }
            }
            try(Reader reader = new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8))
            {
                return GSON.fromJson(reader, clazz);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> @Nullable T read()
    {
        if(!isJson())
        {
            try(FileInputStream fileIn = new FileInputStream(this.file);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn))
            {
                return (T) objectIn.readObject();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("JSON reading requires specifying a target class type context via read(Class<T>).");
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull List<T> readList(Class<T[]> clazz)
    {
        try
        {
            if(!isJson())
            {
                try(FileInputStream fileIn = new FileInputStream(this.file);
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn))
                {
                    return (List<T>) objectIn.readObject();
                }
            }
            try(Reader reader = new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8))
            {
                T[] array = GSON.fromJson(reader, clazz);
                return array == null ? new ArrayList<>() : new ArrayList<>(List.of(array));
            }
        }
        catch(Exception var4)
        {
            return new ArrayList<>();
        }
    }
}