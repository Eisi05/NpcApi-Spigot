package de.eisi05.npc.api.objects;

import de.eisi05.npc.api.utils.SerializableFunction;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the name of an NPC, which can be either a fixed {@link WrappedComponent}
 * or dynamically generated based on a {@link Player}.
 */
public class NpcName implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final WrappedComponent.SerializedComponent nameComponentSerialized;
    private final SerializableFunction<Player, WrappedComponent.SerializedComponent> nameFunctionSerialized;

    private transient final WrappedComponent nameComponent;
    private transient final SerializableFunction<Player, WrappedComponent> nameFunction;

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
     * The {@code nameFunction} generates the name for a player, but if needed,
     * {@code fallback} will be used as a default static name.
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
     * The string is parsed using {@link WrappedComponent#parseFromLegacy(String)}
     * to support Minecraft-style color codes and formatting.
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
        if(nameFunctionSerialized == null)
            return new NpcName(nameComponentSerialized.deserialize());

        return new NpcName(player -> nameFunctionSerialized.apply(player).deserialize(), nameComponentSerialized.deserialize());
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
        return isStatic() ? new NpcName(nameComponent) : new NpcName(nameFunction, nameComponent);
    }

    @Override
    public String toString()
    {
        return "{" + (isStatic() ? "static" : "dynamic") + " -> " + getName().toLegacy(false) + "}";
    }
}
