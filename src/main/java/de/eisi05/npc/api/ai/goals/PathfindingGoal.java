package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.pathfinding.algorithm.AbstractPathfinder;
import org.jetbrains.annotations.NotNull;

/**
 * A goal that uses pathfinding to move an entity to a target location.
 */
public abstract class PathfindingGoal extends Goal
{
    protected AbstractPathfinder.PathfinderFactory<? extends AbstractPathfinder> pathfinder = NpcApi.config.pathfinderFactory();

    /**
     * Creates a new goal with the specified priority.
     *
     * @param priority The priority level for this goal
     */
    protected PathfindingGoal(@NotNull Priority priority)
    {
        super(priority);
    }

    /**
     * Creates a new goal with the same priority and pathfinder as the given goal.
     *
     * @param goal The goal to copy
     */
    protected PathfindingGoal(@NotNull PathfindingGoal goal)
    {
        super(goal.getPriority());
        this.pathfinder = goal.pathfinder;
    }

    /**
     * Sets the pathfinder factory to be used by this goal.
     *
     * @param pathfinder The pathfinder factory to use
     */
    public void setPathfinder(@NotNull AbstractPathfinder.PathfinderFactory<? extends AbstractPathfinder> pathfinder)
    {
        this.pathfinder = pathfinder;
    }

    /**
     * Returns the pathfinder factory currently set for this goal.
     *
     * @return The pathfinder factory
     */
    public @NotNull AbstractPathfinder.PathfinderFactory<? extends AbstractPathfinder> getPathfinder()
    {
        return pathfinder;
    }
}
