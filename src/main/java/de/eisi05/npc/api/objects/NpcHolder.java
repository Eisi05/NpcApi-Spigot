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
import java.util.UUID;

/**
 * Abstract class representing an entity that can hold NPC-related data and has a concept of unsaved changes. It implements {@link InventoryHolder} but onlay as
 * placeholder.
 */
public abstract class NpcHolder implements InventoryHolder
{
    protected static final UUID GLOBAL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    protected final Map<UUID, Map<NpcOption<?, ?>, Object>> options = new HashMap<>();

    /**
     * Flag indicating whether there are unsaved changes to this holder. Defaults to {@code false}.
     */
    private volatile boolean unsavedChanges = false;

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


    public <T> void setOption(@NotNull NpcOption<T, ?> option, @Nullable T value)
    {
        setOption(option, value, GLOBAL_UUID);
    }

    public <T> void setOption(@NotNull NpcOption<T, ?> option, @Nullable T value, @NotNull UUID uuid)
    {
        Map<NpcOption<?, ?>, Object> playerOptions = options.computeIfAbsent(uuid, k -> new HashMap<>());
        if(value == null)
        {
            playerOptions.remove(option);
            if(playerOptions.isEmpty() && uuid.equals(GLOBAL_UUID))
                options.remove(uuid);
        }
        else
            playerOptions.put(option, value);

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

            npc.viewers.forEach(uuid1 ->
            {
                Player player = Bukkit.getPlayer(uuid1);
                if(player == null)
                    return;

                option.getPacket(value, npc, player).ifPresent(packetWrapper -> WrappedServerPlayer.fromPlayer(player).sendPacket(packetWrapper));
            });
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getOption(@NotNull NpcOption<T, ?> option, @NotNull UUID uuid)
    {
        Map<NpcOption<?, ?>, Object> playerOptions = options.get(uuid);
        if(playerOptions != null && playerOptions.containsKey(option))
            return (T) playerOptions.get(option);

        return option.getDefaultValue();
    }

    public <T> @Nullable T getOption(@NotNull NpcOption<T, ?> option)
    {
        return getOption(option, GLOBAL_UUID);
    }


    public void applyOptions(@NotNull Map<NpcOption<?, ?>, Object> options, @NotNull UUID uuid)
    {
        Map<NpcOption<?, ?>, Object> playerOptions = this.options.computeIfAbsent(uuid, k -> new HashMap<>());
        playerOptions.putAll(options);
        if(NpcApi.config.autoUpdate() && this instanceof NPC npc)
            npc.reload();
    }

    public void applyOptions(@NotNull Map<NpcOption<?, ?>, Object> options)
    {
        applyOptions(options, GLOBAL_UUID);
    }

    public @NotNull Map<NpcOption<?, ?>, Object> getOptions(@NotNull UUID uuid)
    {
        return options.getOrDefault(uuid, new HashMap<>());
    }

    public @NotNull Map<NpcOption<?, ?>, Object> getOptions()
    {
        return getOptions(GLOBAL_UUID);
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
