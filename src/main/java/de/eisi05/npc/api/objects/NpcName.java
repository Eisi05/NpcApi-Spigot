package de.eisi05.npc.api.objects;

import de.eisi05.npc.api.utils.SerializableFunction;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Represents the name of an NPC, which can be either a fixed {@link WrappedComponent}
 * or dynamically generated based on a {@link Player}.
 */
public class NpcName
{
    private final WrappedComponent nameComponent;
    private final SerializableFunction<Player, WrappedComponent> nameFunction;

    /**
     * Creates a static NPC name.
     *
     * @param nameComponent the fixed name component
     */
    private NpcName(@NotNull WrappedComponent nameComponent)
    {
        this.nameComponent = nameComponent;
        this.nameFunction = null;
    }

    /**
     * Creates a dynamic NPC name.
     *
     * @param nameFunction a function that returns a name component for a given player
     */
    private NpcName(@NotNull SerializableFunction<Player, WrappedComponent> nameFunction)
    {
        this.nameComponent = null;
        this.nameFunction = nameFunction;
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
     * Creates a new {@link NpcName} with a dynamic name.
     *
     * @param nameFunction a function that returns a name component for a given player
     * @return a new NpcName instance
     */
    public static @NotNull NpcName of(@NotNull SerializableFunction<Player, WrappedComponent> nameFunction)
    {
        return new NpcName(nameFunction);
    }

    /**
     * Checks if this NPC name is static (fixed) or dynamic.
     *
     * @return true if the name is static, false if dynamic
     */
    public boolean isStatic()
    {
        return nameComponent != null;
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
    public @Nullable WrappedComponent getName(@NotNull Player player)
    {
        if(nameFunction == null)
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
        return isStatic() ? new NpcName(nameComponent) : new NpcName(nameFunction);
    }

    /**
     * Converts this NpcName into a serializable version where all WrappedComponents
     * are converted to {@link WrappedComponent.SerializedComponent}.
     *
     * @return a {@link SerializableNpcName} instance
     */
    public @NotNull SerializableNpcName serialize()
    {
        return isStatic() ? new SerializableNpcName(nameComponent.serialize()) :
                new SerializableNpcName(player -> nameFunction.apply(player).serialize());
    }

    /**
     * Serializable representation of an NPC name.
     * Contains either a static {@link WrappedComponent.SerializedComponent} or a dynamic
     * function producing serialized components for each player.
     */
    public static class SerializableNpcName implements Serializable
    {
        private final WrappedComponent.SerializedComponent nameComponent;
        private final SerializableFunction<Player, WrappedComponent.SerializedComponent> nameFunction;

        /**
         * Creates a static serializable NPC name.
         *
         * @param nameComponent the fixed serialized component
         */
        SerializableNpcName(@NotNull WrappedComponent.SerializedComponent nameComponent)
        {
            this.nameComponent = nameComponent;
            this.nameFunction = null;
        }

        /**
         * Creates a dynamic serializable NPC name.
         *
         * @param nameFunction a function returning serialized components for each player
         */
        SerializableNpcName(@NotNull SerializableFunction<Player, WrappedComponent.SerializedComponent> nameFunction)
        {
            this.nameComponent = null;
            this.nameFunction = nameFunction;
        }

        /**
         * Converts this {@link SerializableNpcName} back into a regular {@link NpcName}.
         *
         * @return a new NpcName instance with the deserialized components
         */
        public @NotNull NpcName deserialize()
        {
            return nameComponent != null ? new NpcName(nameComponent.deserialize()) : new NpcName(player -> nameFunction.apply(player).deserialize());
        }
    }
}
