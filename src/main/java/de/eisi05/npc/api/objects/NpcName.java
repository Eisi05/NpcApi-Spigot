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

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Represents the name of an NPC, which can be either a fixed {@link WrappedComponent} or dynamically generated based on a {@link Player}.
 */
@JsonAdapter(NpcName.NpcNameAdapter.class)
public class NpcName implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final WrappedComponent.SerializedComponent nameComponentSerialized;
    private transient final WrappedComponent nameComponent;
    private SerializableFunction<Player, WrappedComponent.SerializedComponent> nameFunctionSerialized;
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
        this.nameFunction = null;

        this.nameComponentSerialized = nameComponent.serialize();
        this.nameFunctionSerialized = null;
    }

    /**
     * Creates a dynamic NPC name with a fallback static component.
     * <p>
     * The {@code nameFunction} generates the name for a player, but if needed, {@code fallback} will be used as a default static name.
     *
     * @param nameFunction the function producing the name for a given player
     * @param fallback     the static fallback name component
     */
    private NpcName(@NotNull SerializableFunction<Player, WrappedComponent> nameFunction, @NotNull WrappedComponent fallback)
    {
        this.nameComponent = fallback;
        this.nameFunction = nameFunction;

        this.nameComponentSerialized = fallback.serialize();
        this.nameFunctionSerialized = player -> nameFunction.apply(player).serialize();
    }

    /**
     * Creates a new {@link NpcName} with a static name.
     *
     * @param name the fixed name component
     * @return a new NpcName instance
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
     * @return a new {@link NpcName} representing the given legacy text
     */
    public static @NotNull NpcName ofLegacy(@NotNull String name)
    {
        return new NpcName(WrappedComponent.parseFromLegacy(name));
    }

    /**
     * Creates a new {@link NpcName} with a dynamic function and a fallback name.
     *
     * @param nameFunction the function producing the name for a given player
     * @param fallback     the static fallback name component
     * @return a new NpcName instance
     */
    public static @NotNull NpcName of(@NotNull SerializableFunction<Player, WrappedComponent> nameFunction, @NotNull WrappedComponent fallback)
    {
        return new NpcName(nameFunction, fallback);
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

    @Serial
    private Object readResolve() throws ObjectStreamException
    {
        NpcName deserialized = nameFunctionSerialized == null ? new NpcName(nameComponentSerialized.deserialize()) :
                new NpcName(player -> nameFunctionSerialized.apply(player).deserialize(), nameComponentSerialized.deserialize());
        if(this.displayOptions != null)
            deserialized.displayOptions = this.displayOptions;
        return deserialized;
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
        return nameFunction == null;
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
     * Gets the NPC name for a specific player.
     *
     * @param player the player to generate the name for
     * @return the name component for the player, or null if this is a static name and no function is defined
     */
    public @Nullable WrappedComponent getName(@Nullable Player player)
    {
        if(nameFunction == null || player == null)
            return nameComponent;

        return nameFunction.apply(player);
    }

    /**
     * Creates a copy of this NpcName instance.
     *
     * @return a new NpcName with the same name component or name function
     */
    public @NotNull NpcName copy()
    {
        NpcName copied = isStatic() ? new NpcName(nameComponent) : new NpcName(nameFunction, nameComponent);
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
