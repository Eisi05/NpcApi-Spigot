package de.eisi05.npc.api.ai;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.objects.NPC;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages and selects goals for an NPC. The goal selector evaluates all available goals each tick and selects the highest priority goal that can be used.
 */
public class GoalSelector
{
    private final NPC npc;
    private final List<Goal> goals;
    private Goal currentGoal;
    private BukkitTask task;
    private boolean running;
    private long tickInterval;

    /**
     * Creates a new GoalSelector for the specified NPC.
     *
     * @param npc The NPC to manage goals for
     */
    public GoalSelector(@NotNull NPC npc)
    {
        this.npc = npc;
        this.goals = new ArrayList<>();
        this.tickInterval = 1L;
    }

    /**
     * Adds a goal to this selector.
     *
     * @param goal The goal to add
     * @return This selector for method chaining
     */
    public @NotNull GoalSelector addGoal(@NotNull Goal goal)
    {
        goals.add(goal);
        return this;
    }

    /**
     * Removes a goal from this selector.
     *
     * @param goal The goal to remove
     * @return This selector for method chaining
     */
    public @NotNull GoalSelector removeGoal(@NotNull Goal goal)
    {
        goals.remove(goal);
        if(currentGoal == goal)
        {
            currentGoal.stop(npc);
            currentGoal = null;
        }
        return this;
    }

    /**
     * Gets all goals registered with this selector.
     *
     * @return A list of all goals
     */
    public @NotNull ArrayList<Goal> getGoals()
    {
        return new ArrayList<>(goals);
    }

    /**
     * Gets the currently active goal.
     *
     * @return The current goal, or null if no goal is active
     */
    public @Nullable Goal getCurrentGoal()
    {
        return currentGoal;
    }

    /**
     * Starts the goal selector. This will begin evaluating and executing goals.
     */
    public void start()
    {
        if(running)
            return;

        running = true;
        task = Bukkit.getScheduler().runTaskTimer(NpcApi.plugin, this::tick, 0L, tickInterval);
    }

    /**
     * Stops the goal selector. This will stop the current goal and cease evaluation.
     */
    public void stop()
    {
        if(!running)
            return;

        running = false;
        if(task != null)
        {
            task.cancel();
            task = null;
        }

        if(currentGoal != null)
        {
            currentGoal.stop(npc);
            currentGoal = null;
        }
    }

    /**
     * Checks whether the goal selector is currently running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * Gets the current tick interval.
     *
     * @return The tick interval in ticks
     */
    public long getTickInterval()
    {
        return tickInterval;
    }

    /**
     * Sets the tick interval for goal evaluation.
     *
     * @param ticks The number of ticks between evaluations (1 = every tick)
     */
    public void setTickInterval(long ticks)
    {
        this.tickInterval = ticks;
        if(running)
        {
            stop();
            start();
        }
    }

    /**
     * Manually forces a specific goal to start, bypassing normal selection. This is useful for triggering goals based on external events.
     *
     * @param goal The goal to force start
     */
    public void forceGoal(@NotNull Goal goal)
    {
        if(!goals.contains(goal))
            return;

        if(currentGoal != null)
            currentGoal.stop(npc);

        currentGoal = goal;
        currentGoal.start(npc);
    }

    /**
     * The main tick method that evaluates and executes goals.
     */
    private void tick()
    {
        Goal selectedGoal = goals.stream()
                .filter(goal -> goal.canUse(npc))
                .max(Comparator.comparingInt(Goal::getPriority))
                .orElse(null);

        if(currentGoal != null)
        {
            if(selectedGoal != null && selectedGoal != currentGoal)
            {
                if(selectedGoal.getPriority() >= currentGoal.getPriority())
                {
                    currentGoal.stop(npc);
                    currentGoal = selectedGoal;
                    currentGoal.start(npc);
                    return;
                }
            }

            if(currentGoal.canContinue(npc))
            {
                currentGoal.tick(npc);
                return;
            }
            else
            {
                currentGoal.stop(npc);
                currentGoal = null;
            }
        }

        if(selectedGoal != null)
        {
            currentGoal = selectedGoal;
            currentGoal.start(npc);
        }
    }
}
