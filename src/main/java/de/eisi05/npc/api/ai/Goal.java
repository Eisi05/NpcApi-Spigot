package de.eisi05.npc.api.ai;

import de.eisi05.npc.api.objects.NPC;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * Base interface for NPC AI goals. Goals represent specific behaviors that an NPC can perform, such as walking to a location, attacking an entity, or looking
 * around.
 */
public interface Goal extends Serializable
{
    @Serial
    long serialVersionUID = 1L;

    /**
     * Checks whether this goal can be used right now. This is called each tick to determine if the goal should be considered for execution.
     *
     * @param npc The NPC to check for
     * @return true if this goal can be used, false otherwise
     */
    boolean canUse(@NotNull NPC npc);

    /**
     * Called when this goal starts executing. Use this to initialize any state needed for the goal.
     *
     * @param npc The NPC starting this goal
     */
    void start(@NotNull NPC npc);

    /**
     * Called each tick while this goal is active. Use this to update the goal's behavior.
     *
     * @param npc The NPC executing this goal
     */
    void tick(@NotNull NPC npc);

    /**
     * Called when this goal stops executing. Use this to clean up any state or cancel ongoing tasks.
     *
     * @param npc The NPC stopping this goal
     */
    void stop(@NotNull NPC npc);

    /**
     * Gets the priority of this goal. Higher priority goals will be selected over lower priority ones. Minecraft uses values like 0-8 typically, with higher
     * being more important.
     *
     * @return The priority of this goal
     */
    int getPriority();

    /**
     * Checks whether this goal can continue executing. If this returns false, the goal will be stopped and a new goal will be selected.
     *
     * @param npc The NPC to check for
     * @return true if this goal can continue, false otherwise
     */
    default boolean canContinue(@NotNull NPC npc)
    {
        return canUse(npc);
    }

    /**
     * Gets whether this goal should be exclusive (no other goals can run simultaneously). By default, goals are exclusive.
     *
     * @return true if this goal is exclusive, false otherwise
     */
    default boolean isExclusive()
    {
        return true;
    }
}
