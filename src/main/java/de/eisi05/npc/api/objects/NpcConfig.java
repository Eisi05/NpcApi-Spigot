package de.eisi05.npc.api.objects;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration settings for NPC behavior.
 * This class allows for customizing various aspects of an NPC, such as interaction timers.
 */
public class NpcConfig
{
    /**
     * The time in ticks an NPC will look at a player after interaction.
     * The default value is 5 ticks.
     */
    private long lookAtTimer = 5;

    /**
     * Sets the duration an NPC will look at a player after an interaction.
     *
     * @param time The time in ticks. For example, 20 ticks = 1 second.
     * @return This {@link NpcConfig} instance for method chaining. Will not be null.
     */
    public @NotNull NpcConfig lookAtTimer(long time)
    {
        lookAtTimer = time;
        return this;
    }

    /**
     * Gets the configured duration an NPC will look at a player.
     *
     * @return The time in ticks.
     */
    public long getLookAtTimer()
    {
        return lookAtTimer;
    }
}
