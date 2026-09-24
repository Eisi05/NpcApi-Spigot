package de.eisi05.npc.api.manager;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.pathfinding.Path;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the combat-related state and death behavior of an NPC.
 *
 * <p>Handles health, knockback, death behavior, respawning, and respawn locations.</p>
 */
public class NpcCombatManager implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean enabled = false;
    private double maxHealth = 20.0; // -1 = invulnerable
    private double currentHealth = 20.0;
    private double knockbackMultiplier = 1.0;
    private DeathBehavior deathBehavior = DeathBehavior.DISABLE;

    private long respawnDelayTicks = 100;
    private Path.SerializablePath.SerializableLocation respawnLocation = null;

    /**
     * Creates a copy of this combat manager with the same configuration and current state.
     *
     * @return a copy of this combat manager
     */
    public @NotNull NpcCombatManager copy()
    {
        NpcCombatManager copy = new NpcCombatManager()
                .setMaxHealth(maxHealth)
                .setKnockbackMultiplier(knockbackMultiplier)
                .setDeathBehavior(deathBehavior)
                .setEnabled(enabled);

        copy.currentHealth = this.currentHealth;
        copy.respawnDelayTicks = this.respawnDelayTicks;
        copy.respawnLocation = this.respawnLocation;
        return copy;
    }

    // --- Death Handling Logic ---

    /**
     * Handles the NPC's death according to the configured death behavior.
     *
     * @param npc the NPC that died
     */
    public void handleDeath(@NotNull NPC npc)
    {
        switch(deathBehavior)
        {
            case DELETE ->
            {
                try
                {
                    npc.delete();
                }
                catch(IOException ignored)
                {
                }
            }
            case RESPAWN ->
            {
                boolean showToAll = npc.getVisibilityManager().shouldShowToAllPlayers();
                Set<UUID> specific = npc.getVisibilityManager().getSpecificPlayers();

                npc.hideNpcFromAllPlayers();

                Location loc = getRespawnLocation(npc.getLocation().getWorld());
                if(loc != null)
                    npc.changeRealLocation(loc);

                Bukkit.getScheduler().runTaskLater(NpcApi.plugin, () ->
                {
                    setCurrentHealth(maxHealth, npc);

                    if(showToAll)
                        npc.showNpcToAllPlayers();
                    else
                    {
                        for(UUID uuid : specific)
                        {
                            Player player = Bukkit.getPlayer(uuid);
                            if(player != null)
                                npc.showNPCToPlayer(player);
                        }
                    }
                }, respawnDelayTicks);
            }
            case DISABLE ->
            {
                setCurrentHealth(maxHealth, npc);
                Location loc = getRespawnLocation(npc.getLocation().getWorld());
                if(loc != null)
                    npc.changeRealLocation(loc);

                npc.setEnabled(false);
            }
            case NONE ->
            {
            }
        }
    }

    // --- Getters & Setters ---

    /**
     * Checks whether combat is enabled.
     *
     * @return {@code true} if combat is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Sets whether combat is enabled.
     *
     * @param enabled {@code true} to enable combat, {@code false} to disable
     */
    public @NotNull NpcCombatManager setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the NPC's current health.
     *
     * @return the current health
     */
    public double getCurrentHealth()
    {
        return currentHealth;
    }

    /**
     * Sets the NPC's current health.
     *
     * <p>If the resulting health is zero or below, the configured death behavior
     * is triggered when an NPC is provided.</p>
     *
     * @param currentHealth the new health value
     * @param npc           the affected NPC, or {@code null} to skip death handling
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setCurrentHealth(double currentHealth, @Nullable NPC npc)
    {
        this.currentHealth = Math.min(currentHealth, maxHealth);

        if(this.currentHealth <= 0 && this.currentHealth != this.maxHealth && npc != null)
            handleDeath(npc);

        return this;
    }

    /**
     * Gets the maximum health of the NPC.
     *
     * @return the maximum health
     */
    public double getMaxHealth()
    {
        return maxHealth;
    }

    /**
     * Sets the maximum health of the NPC.
     *
     * @param maxHealth the new maximum health
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setMaxHealth(double maxHealth)
    {
        this.maxHealth = maxHealth;
        this.currentHealth = Math.min(this.currentHealth, maxHealth);
        return this;
    }

    /**
     * Checks whether the NPC is invulnerable.
     *
     * @return {@code true} if the maximum health is negative
     */
    public boolean isInvulnerable()
    {
        return maxHealth < 0;
    }

    /**
     * Gets the knockback multiplier applied to the NPC.
     *
     * @return the knockback multiplier
     */
    public double getKnockbackMultiplier()
    {
        return knockbackMultiplier;
    }

    /**
     * Sets the knockback multiplier applied to the NPC.
     *
     * @param knockbackMultiplier the new knockback multiplier
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setKnockbackMultiplier(double knockbackMultiplier)
    {
        this.knockbackMultiplier = Math.max(knockbackMultiplier, 0);
        return this;
    }

    /**
     * Gets the configured death behavior.
     *
     * @return the death behavior
     */
    public @NotNull DeathBehavior getDeathBehavior()
    {
        return deathBehavior;
    }

    /**
     * Sets the death behavior of the NPC.
     *
     * @param deathBehavior the new death behavior
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setDeathBehavior(@NotNull DeathBehavior deathBehavior)
    {
        this.deathBehavior = deathBehavior;
        return this;
    }

    /**
     * Gets the configured respawn delay.
     *
     * @return the respawn delay in ticks
     */
    public long getRespawnDelayTicks()
    {
        return respawnDelayTicks;
    }

    /**
     * Sets the respawn delay.
     *
     * @param respawnDelayTicks the respawn delay in ticks
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setRespawnDelayTicks(long respawnDelayTicks)
    {
        this.respawnDelayTicks = respawnDelayTicks;
        return this;
    }

    /**
     * Gets the configured respawn location.
     *
     * @param fallback the fallback world used when converting the serialized location
     * @return the respawn location, or {@code null} if unset
     */
    public @Nullable Location getRespawnLocation(@Nullable World fallback)
    {
        if(respawnLocation == null)
            return null;

        return respawnLocation.toLocation(fallback);
    }

    /**
     * Sets the respawn location.
     *
     * @param respawnLocation the new respawn location, or {@code null} to unset it
     * @return this combat manager
     */
    public @NotNull NpcCombatManager setRespawnLocation(@Nullable Location respawnLocation)
    {
        this.respawnLocation = respawnLocation == null ? null : new Path.SerializablePath.SerializableLocation(respawnLocation);
        return this;
    }

    /**
     * Defines how an NPC should behave when its health reaches zero.
     */
    public enum DeathBehavior
    {
        /**
         * Permanently deletes the NPC via npc.delete().
         */
        DELETE,

        /**
         * Hides the NPC, waits for a delay, resets health/location, and shows it again.
         */
        RESPAWN,

        /**
         * Disables the NPC without removing it from memory, resetting health/location.
         */
        DISABLE,

        /**
         * Does nothing automatically; leaves handling entirely to external custom events.
         */
        NONE
    }
}