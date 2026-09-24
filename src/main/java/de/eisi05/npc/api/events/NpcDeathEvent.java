package de.eisi05.npc.api.events;

import de.eisi05.npc.api.objects.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an NPC dies.
 * <p>
 * This event can be cancelled to prevent the NPC from dying.
 */
public class NpcDeathEvent extends Event implements Cancellable
{
    private static final HandlerList HANDLERS = new HandlerList();

    private final NPC npc;
    private final Player killer;
    private boolean cancelled;

    /**
     * Creates a new NPC death event.
     *
     * @param npc    the NPC that died
     * @param killer the player who killed the NPC
     */
    public NpcDeathEvent(@NotNull NPC npc, @NotNull Player killer)
    {
        this.npc = npc;
        this.killer = killer;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the event's handler list
     */
    public static @NotNull HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    /**
     * Gets the NPC that died.
     *
     * @return the dead NPC
     */
    public @NotNull NPC getNpc()
    {
        return npc;
    }

    /**
     * Gets the player who killed the NPC.
     *
     * @return the killer
     */
    public @NotNull Player getKiller()
    {
        return killer;
    }

    /**
     * Checks whether this event is cancelled.
     *
     * @return {@code true} if cancelled, otherwise {@code false}
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Sets whether this event is cancelled.
     *
     * @param cancel {@code true} to cancel the event, {@code false} otherwise
     */
    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the event's handler list
     */
    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }
}