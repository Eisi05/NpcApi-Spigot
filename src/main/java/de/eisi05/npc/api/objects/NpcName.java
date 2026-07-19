package de.eisi05.npc.api.objects;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import de.eisi05.npc.api.utils.SerializableFunction;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Represents the name of an NPC, which can be either a fixed {@link WrappedComponent} or dynamically generated based on a {@link Player}.
 */
@JsonAdapter(NpcName.NpcNameAdapter.class)
public class NpcName implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Map<String, BiFunction<Player, String, WrappedComponent>> REGISTRY = new ConcurrentHashMap<>();
    private WrappedComponent.SerializedComponent nameComponentSerialized;
    private String nameFunctionKey;
    private transient WrappedComponent nameComponent;
    private transient SerializableFunction<Player, WrappedComponent.SerializedComponent> nameFunctionSerialized;
    private transient SerializableFunction<Player, WrappedComponent> nameFunction;
    private NameDisplayOptions displayOptions = new NameDisplayOptions();

    /**
     * Creates a static NPC name.
     *
     * @param nameComponent the fixed name component
     */
    private NpcName(@NotNull WrappedComponent nameComponent)
    {
        this.nameComponent = nameComponent;
        this.nameComponentSerialized = nameComponent.serialize();
        this.nameFunctionKey = null;
    }

    /**
     * Creates a dynamic NPC name with a lookup key and a fallback static component.
     *
     * @param nameFunctionKey the registry key used to resolve the dynamic function at runtime
     * @param fallback        the static fallback name component used if the key is missing or generation fails
     */
    private NpcName(@NotNull String nameFunctionKey, @NotNull WrappedComponent fallback)
    {
        this.nameComponent = fallback;
        this.nameComponentSerialized = fallback.serialize();
        this.nameFunctionKey = nameFunctionKey;
    }

    /**
     * Registers a dynamic name generation function globally. Call this inside your JavaPlugin's {@code onEnable()} method.
     *
     * @param key      the unique identifier for the function (case-insensitive)
     * @param function the function producing the name component given the viewer player and the fallback legacy text
     * @throws IllegalArgumentException if the provided key is already registered
     */
    public static void registerFunction(@NotNull String key, @NotNull BiFunction<Player, String, WrappedComponent> function) throws IllegalArgumentException
    {
        if(REGISTRY.containsKey(key.toLowerCase()))
            throw new IllegalArgumentException("Key " + key + " is already registered!");

        REGISTRY.put(key.toLowerCase(), function);
    }

    /**
     * Creates a new {@link NpcName} with a static name.
     *
     * @param name the fixed name component
     * @return a new static NpcName instance
     */
    public static @NotNull NpcName of(@NotNull WrappedComponent name)
    {
        return new NpcName(name);
    }

    /**
     * Creates a new {@link NpcName} from a legacy text string.
     * <p>
     * The string is parsed using {@link WrappedComponent#parseFromLegacy(String)} to support Minecraft-style color codes and formatting.
     *
     * @param name the legacy text string to convert into an NPC name
     * @return a new static {@link NpcName} representing the given legacy text
     */
    public static @NotNull NpcName ofLegacy(@NotNull String name)
    {
        return new NpcName(WrappedComponent.parseFromLegacy(name));
    }

    /**
     * Creates a new {@link NpcName} with a dynamic function key and a fallback name.
     *
     * @param functionKey the registry key used to look up the function at runtime
     * @param fallback    the static fallback name component
     * @return a new dynamic NpcName instance
     */
    public static @NotNull NpcName of(@NotNull String functionKey, @NotNull WrappedComponent fallback)
    {
        return new NpcName(functionKey, fallback);
    }

    /**
     * Creates an empty NPC name.
     * <p>
     * This returns an {@link NpcName} with a {@link WrappedComponent} containing no content.
     *
     * @return a new NpcName representing an empty name
     */
    public static @NotNull NpcName empty()
    {
        return NpcName.of(WrappedComponent.create(null));
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        ObjectInputStream.GetField fields;
        try
        {
            fields = in.readFields();
        }
        catch(Throwable t)
        {
            this.nameComponent = WrappedComponent.create("Legacy NPC");
            this.nameFunctionKey = null;
            return;
        }

        this.nameComponentSerialized = (WrappedComponent.SerializedComponent) fields.get("nameComponentSerialized", null);
        this.nameComponent = this.nameComponentSerialized != null ? this.nameComponentSerialized.deserialize() : WrappedComponent.create(null);
        this.displayOptions = (NameDisplayOptions) fields.get("displayOptions", new NameDisplayOptions());

        boolean isModernFormat = fields.getObjectStreamClass().getField("nameFunctionKey") != null;

        if(isModernFormat)
        {
            this.nameFunctionKey = (String) fields.get("nameFunctionKey", null);
            return;
        }

        SerializableFunction<Player, WrappedComponent.SerializedComponent> oldStringFunc;
        try
        {
            oldStringFunc = (SerializableFunction<Player, WrappedComponent.SerializedComponent>) fields.get("nameFunctionSerialized", null);
        }
        catch(Throwable t)
        {
            oldStringFunc = null;
        }

        if(oldStringFunc != null)
            this.nameFunctionKey = "placeholder";
        else
            this.nameFunctionKey = null;
    }

    /**
     * Gets the display options for this NPC name.
     *
     * @return the display options
     */
    public @NotNull NameDisplayOptions getDisplayOptions()
    {
        return this.displayOptions;
    }

    /**
     * Sets the display options for this NPC name.
     *
     * @param displayOptions the display options to set
     */
    public void setDisplayOptions(@NotNull NameDisplayOptions displayOptions)
    {
        this.displayOptions = displayOptions;
    }

    /**
     * Checks if this NPC name is static (fixed) or dynamic.
     *
     * @return true if the name is static, false if dynamic
     */
    public boolean isStatic()
    {
        return nameFunctionKey == null;
    }

    /**
     * Gets the static name component, if present.
     *
     * @return the static name component, or null if the name is dynamic
     */
    public @Nullable WrappedComponent getName()
    {
        return nameComponent;
    }

    /**
     * Gets the contextual NPC name for a specific player, fallback to the static name if applicable.
     *
     * @param player the viewing player to evaluate the dynamic function for
     * @return the generated dynamic component, or the static fallback component if the name is static, the player is null, or function execution fails.
     */
    public @Nullable WrappedComponent getName(@Nullable Player player)
    {
        if(isStatic() || player == null)
            return getName();

        BiFunction<Player, String, WrappedComponent> runtimeFunc = REGISTRY.get(nameFunctionKey.toLowerCase());
        if(runtimeFunc != null)
        {
            try
            {
                return runtimeFunc.apply(player, nameComponent.toLegacy(true));
            }
            catch(Exception e)
            {
            }
        }

        return getName();
    }

    /**
     * Creates a copy of this NpcName instance.
     *
     * @return a new NpcName with the same name component or name function
     */
    public @NotNull NpcName copy()
    {
        NpcName copied = isStatic() ? new NpcName(nameComponent) : new NpcName(nameFunctionKey, nameComponent);
        copied.displayOptions = this.displayOptions.copy();
        return copied;
    }

    @Override
    public String toString()
    {
        return "{" + (isStatic() ? "static" : "dynamic") + " -> " + getName().toLegacy(false) + "}";
    }

    static class NpcNameAdapter implements JsonSerializer<NpcName>, JsonDeserializer<NpcName>
    {
        @Override
        public JsonElement serialize(NpcName src, Type typeOfSrc, JsonSerializationContext context)
        {
            if(src == null)
                return JsonNull.INSTANCE;

            JsonObject obj = new JsonObject();

            if (src.getName() != null)
            {
                try
                {
                    String rawComponentJson = Var.toJson(src.getName());
                    if (rawComponentJson != null && !rawComponentJson.isEmpty())
                        obj.add("component", JsonParser.parseString(rawComponentJson));
                }
                catch (Exception e)
                {
                    obj.add("component", JsonNull.INSTANCE);
                }
            }

            try
            {
                Object funcSerialized = src.nameFunctionSerialized;
                if(funcSerialized != null)
                    obj.add("nameFunctionSerialized", context.serialize(funcSerialized));
            }
            catch(Exception ignored) {}

            if(src.getDisplayOptions() != null)
                obj.add("displayOptions", context.serialize(src.getDisplayOptions(), NameDisplayOptions.class));

            return obj;
        }

        @Override
        public NpcName deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if(json == null || json.isJsonNull())
                return null;

            JsonObject obj = json.getAsJsonObject();
            WrappedComponent component = null;

            if(obj.has("component"))
            {
                JsonElement nameCompElement = obj.get("component");
                if (!nameCompElement.isJsonNull())
                    component = Var.fromJson(nameCompElement.toString());
            }

            if(component == null)
                component = WrappedComponent.create(null);

            NpcName npcName = NpcName.of(component);
            if(obj.has("nameFunctionSerialized"))
            {
                Type funcType = new TypeToken<SerializableFunction<Player, WrappedComponent.SerializedComponent>>() {}.getType();
                SerializableFunction<Player, WrappedComponent.SerializedComponent> funcSerialized =
                        context.deserialize(obj.get("nameFunctionSerialized"), funcType);

                if(funcSerialized != null)
                {
                    npcName.nameFunctionSerialized = funcSerialized;
                    npcName.nameFunction = player ->
                    {
                        WrappedComponent.SerializedComponent serializedComp = funcSerialized.apply(player);
                        return serializedComp != null ? serializedComp.deserialize() : null;
                    };
                }
            }

            if(obj.has("displayOptions"))
            {
                NameDisplayOptions options = context.deserialize(obj.get("displayOptions"), NameDisplayOptions.class);
                if(options != null)
                    npcName.displayOptions = options;
            }

            return npcName;
        }
    }
}
