package de.eisi05.npc.api.utils.serialize;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.interfaces.NpcClickAction;
import de.eisi05.npc.api.objects.NpcName;
import de.eisi05.npc.api.objects.NpcSkin;
import de.eisi05.npc.api.utils.SerializableFunction;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class ObjectSaver
{
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

    private static final TypeAdapter<ItemStack> ITEM_STACK_ADAPTER = new TypeAdapter<ItemStack>()
    {
        @Override
        public void write(JsonWriter out, ItemStack value) throws IOException
        {
            if(value == null || value.getType().isAir() || value.getAmount() <= 0)
            {
                out.nullValue();
                return;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.set("item", value);

            String yamlString = config.saveToString();
            Map<String, Object> nativeMap = new org.yaml.snakeyaml.Yaml().load(yamlString);
            Object itemStructure = nativeMap.get("item");

            JsonElement element = CLEAN_GSON.toJsonTree(itemStructure);
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

            JsonElement element = Streams.parse(in);
            if(!element.isJsonObject())
                return null;

            JsonObject jsonObject = element.getAsJsonObject();
            YamlConfiguration config = new YamlConfiguration();
            try
            {
                config.loadFromString("item: " + CLEAN_GSON.toJson(jsonObject));
            }
            catch(InvalidConfigurationException e)
            {
                throw new IOException("Failed to parse JSON into Bukkit Configuration", e);
            }

            return config.getItemStack("item");
        }
    }.nullSafe();

    private static final TypeAdapter<Serializable> SERIALIZABLE_ADAPTER = new TypeAdapter<>()
    {
        @Override
        public void write(JsonWriter out, Serializable value) throws IOException
        {
            if(value == null)
            {
                out.nullValue();
                return;
            }
            CLEAN_GSON.toJson(value, value.getClass(), out);
        }

        @Override
        public Serializable read(JsonReader in)
        {
            JsonElement element = JsonParser.parseReader(in);
            return (Serializable) CLEAN_GSON.fromJson(element, Object.class);
        }
    };

    static final Gson CLEAN_GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new GsonDebugFactory())
            .registerTypeAdapter(Instant.class, INSTANT_ADAPTER)
            .registerTypeHierarchyAdapter(ItemStack.class, ITEM_STACK_ADAPTER)
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    @SuppressWarnings("unchecked")
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new GsonDebugFactory())
            .registerTypeAdapter(Instant.class, INSTANT_ADAPTER)
            .registerTypeHierarchyAdapter(ItemStack.class, ITEM_STACK_ADAPTER)
            .registerTypeAdapter(Serializable.class, SERIALIZABLE_ADAPTER)
            .registerTypeHierarchyAdapter(NpcClickAction.class, new NpcClickActionAdapter())
            .registerTypeHierarchyAdapter(Goal.class, (JsonDeserializer<Goal>) (json, typeOfT, context) ->
            {
                JsonObject jsonObject = json.getAsJsonObject();
                if(jsonObject.entrySet().isEmpty())
                    return null;
                Map.Entry<String, JsonElement> entry = jsonObject.entrySet().iterator().next();
                String goalKey = entry.getKey();
                JsonObject goalData = entry.getValue().getAsJsonObject();
                String className = goalKey.substring(0, 1).toUpperCase() + goalKey.substring(1) + "Goal";
                try
                {
                    Class<?> clazz = Class.forName("de.eisi05.npc.api.ai.goals." + className);
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
                        throw new JsonParseException("Unknown goal: " + className + " (" + goalKey + ")", ex);
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
            .registerTypeAdapter(new TypeToken<HashMap<String, Serializable>>() {}.getType(),
                    (JsonDeserializer<HashMap<String, Serializable>>) (json, typeOfT, context) ->
                    {
                        HashMap<String, Serializable> map = new HashMap<>();
                        JsonObject obj = json.getAsJsonObject();

                        for(Map.Entry<String, JsonElement> entry : obj.entrySet())
                        {
                            String key = entry.getKey();
                            JsonElement element = entry.getValue();

                            if (key.equalsIgnoreCase("goals"))
                            {
                                ArrayList<Goal> goals = context.deserialize(element, new TypeToken<ArrayList<Goal>>(){}.getType());
                                map.put(key, goals);
                            }
                            else if (key.equalsIgnoreCase("skin"))
                            {
                                NpcSkin skin = context.deserialize(element, NpcSkin.class);
                                map.put(key, skin);
                            }
                            else if(key.equalsIgnoreCase("equipment"))
                            {
                                HashMap<EquipmentSlot, ItemStack> equipment = context.deserialize(element, new TypeToken<HashMap<EquipmentSlot, ItemStack>>(){}.getType());
                                map.put(key, equipment);
                            }
                            else
                            {
                                Serializable val = context.deserialize(element, Serializable.class);
                                map.put(key, val);
                            }
                        }
                        return map;
                    })
            .registerTypeAdapter(new TypeToken<Map<String, HashMap<String, Serializable>>>() {}.getType(),
                    (JsonDeserializer<Map<String, HashMap<String, Serializable>>>) (json, typeOfT, context) ->
                    {
                        HashMap<String, HashMap<String, Serializable>> map = new HashMap<>();
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
                            map.put(key.toString(), val);
                        }
                        return map;
                    })
            .registerTypeAdapter(new TypeToken<Map<String, HashMap<String, Serializable>>>() {}.getType(),
                    (JsonSerializer<Map<String, HashMap<String, Serializable>>>) (src, typeOfSrc, context) ->
                    {
                        JsonObject obj = new JsonObject();
                        UUID globalUuid = new UUID(0L, 0L);

                        src.forEach((key, value) ->
                        {
                            String keyStr = key.equals(globalUuid.toString()) ? "global" : key.toString();
                            JsonElement valElement = context.serialize(value);
                            obj.add(keyStr, valElement);
                        });
                        return obj;
                    })
            .registerTypeAdapter(NpcName.class, (JsonDeserializer<NpcName>) (json, typeOfT, context) ->
            {
                NpcName raw = CLEAN_GSON.fromJson(json, NpcName.class);
                if(raw == null)
                    return null;

                try
                {
                    Field nameComponentSerializedField = NpcName.class.getDeclaredField("nameComponentSerialized");
                    Field nameFunctionSerializedField = NpcName.class.getDeclaredField("nameFunctionSerialized");
                    Field nameComponentField = NpcName.class.getDeclaredField("nameComponent");
                    Field nameFunctionField = NpcName.class.getDeclaredField("nameFunction");

                    nameComponentSerializedField.setAccessible(true);
                    nameFunctionSerializedField.setAccessible(true);
                    nameComponentField.setAccessible(true);
                    nameFunctionField.setAccessible(true);

                    var compSerialized = (WrappedComponent.SerializedComponent) nameComponentSerializedField.get(raw);
                    var funcSerialized = (SerializableFunction<Player, ?>) nameFunctionSerializedField.get(raw);

                    if(compSerialized != null)
                        nameComponentField.set(raw, compSerialized.deserialize());
                    if(funcSerialized != null)
                    {
                        nameFunctionField.set(raw, (SerializableFunction<Player, Object>) player ->
                        {
                            var res = (WrappedComponent.SerializedComponent) funcSerialized.apply(player);
                            return res != null ? res.deserialize() : null;
                        });
                    }
                }
                catch(Exception e)
                {
                    throw new JsonParseException("Failed to post-deserialize NpcName transient fields", e);
                }

                return raw;
            })
            .disableHtmlEscaping()
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
            file.getParentFile().mkdirs();
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