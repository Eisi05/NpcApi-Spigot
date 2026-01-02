package de.eisi05.npc.api.objects;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class representing an entity that can hold NPC-related data and has a concept of unsaved changes. It implements {@link InventoryHolder} but onlay as
 * placeholder.
 */
public abstract class NpcHolder implements InventoryHolder
{
    protected final Map<NpcOption<?, ?>, Object> options = new HashMap<>();

    /**
     * Flag indicating whether there are unsaved changes to this holder. Defaults to {@code false}.
     */
    private boolean unsavedChanges = false;

    /**
     * Checks if there are any unsaved changes for this NPC holder.
     *
     * @return {@code true} if there are unsaved changes, {@code false} otherwise.
     */

    public boolean hasUnsavedChanges()
    {
        return unsavedChanges;
    }

    /**
     * Marks that there are unsaved changes to this NPC holder. This should be called whenever a modifiable property of the holder is changed.
     */
    public void markChange()
    {
        unsavedChanges = true;
    }

    /**
     * Saves the current state of the NPC holder. This method is intended to persist any changes. After successful execution, the {@code unsavedChanges} flag is
     * reset to {@code false}.
     *
     * @throws IOException if an error occurs during the saving process.
     */
    public void save() throws IOException
    {
        unsavedChanges = false;
    }

    /**
     * Sets a specific option for this NPC.
     *
     * @param option the {@link NpcOption} to set. Must not be null.
     * @param value  the value for the option. If {@code null}, the option will be removed (reverting to default).
     * @param <T>    the type of the option's value.
     */
    public <T> void setOption(@NotNull NpcOption<T, ?> option, @Nullable T value)
    {
        if(value == null)
            options.remove(option);
        else
            options.put(option, value);

        if(NpcApi.config.autoUpdate() && this instanceof NPC npc)
        {
            if(option.equals(NpcOption.SKIN) || option.equals(NpcOption.USE_PLAYER_SKIN))
            {
                npc.updateSkin(player -> true);
                return;
            }

            if(option.equals(NpcOption.ENTITY))
            {
                npc.reload();
                return;
            }

            npc.viewers.forEach(uuid ->
            {
                Player player = Bukkit.getPlayer(uuid);
                if(player == null)
                    return;

                option.getPacket(value, npc, player).ifPresent(packetWrapper -> WrappedServerPlayer.fromPlayer(player).sendPacket(packetWrapper));
            });
        }
    }

    /**
     * Gets the value of a specific option for this NPC. If the option has not been explicitly set, its default value will be returned.
     *
     * @param option the {@link NpcOption} to get. Must not be null.
     * @param <T>    the type of the option's value.
     * @return the value of the option.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getOption(@NotNull NpcOption<T, ?> option)
    {
        return (T) options.getOrDefault(option, option.getDefaultValue());
    }

    /**
     * Applies the given NPC options to this instance and optionally triggers an update.
     * <p>
     * All entries in the provided map are merged into the current option set. If an option already exists, its value will be overwritten by the new value.
     * Options not present in the provided map remain unchanged.
     * <p>
     * If automatic updates are enabled and this instance represents an {@link NPC}, the NPC will be reloaded after the options have been applied.
     *
     * @param options A map containing NPC options to apply and their associated values. Must not be {@code null}.
     */
    public void applyOptions(@NotNull Map<NpcOption<?, ?>, Object> options)
    {
        this.options.putAll(options);
        if(NpcApi.config.autoUpdate() && this instanceof NPC npc)
            npc.reload();
    }

    /**
     * Returns an immutable view of the currently applied NPC options.
     * <p>
     * The returned map represents the full set of options that are currently active for this NPC instance.
     *
     * @return A map of all active NPC options and their values.
     */
    public @NotNull Map<NpcOption<?, ?>, Object> getOptions()
    {
        return options;
    }

    /**
     * @throws UnsupportedOperationException always, as this inventory is not used.
     */
    @Override
    public @NotNull Inventory getInventory()
    {
        throw new UnsupportedOperationException("This inventory is not used!");
    }
}
