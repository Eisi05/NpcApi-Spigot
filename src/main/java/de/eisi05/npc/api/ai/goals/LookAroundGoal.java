package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.objects.NPC;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A goal that makes the NPC look around randomly. This can serve as a "wait" goal or an idle behavior when no other goals are active.
 */
public class LookAroundGoal implements Goal
{
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MIN_DURATION = 40; // 2 seconds
    private static final int DEFAULT_MAX_DURATION = 100; // 5 seconds
    private static final float YAW_CHANGE_MAX = 45f;
    private static final float PITCH_CHANGE_MAX = 20f;

    private final int minDuration;
    private final int maxDuration;

    private transient int ticksRemaining;
    private transient float targetYaw;
    private transient float targetPitch;
    private transient boolean isLooking;

    /**
     * Creates a LookAroundGoal with default duration.
     */
    public LookAroundGoal()
    {
        this(DEFAULT_MIN_DURATION, DEFAULT_MAX_DURATION);
    }

    /**
     * Creates a LookAroundGoal with custom duration range.
     *
     * @param minDuration Minimum duration in ticks
     * @param maxDuration Maximum duration in ticks
     */
    public LookAroundGoal(int minDuration, int maxDuration)
    {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean canUse(@NotNull NPC npc)
    {
        return true;
    }

    @Override
    public void start(@NotNull NPC npc)
    {
        ticksRemaining = minDuration + ThreadLocalRandom.current().nextInt(maxDuration - minDuration);
        isLooking = true;
        pickNewLookDirection(npc);
    }

    @Override
    public void tick(@NotNull NPC npc)
    {
        if(ticksRemaining <= 0)
        {
            pickNewLookDirection(npc);
            ticksRemaining = minDuration + ThreadLocalRandom.current().nextInt(maxDuration - minDuration);
        }

        Location currentLoc = npc.getLocation();
        float currentYaw = currentLoc.getYaw();
        float currentPitch = currentLoc.getPitch();

        //TODO: Improve look around angle
        float newYaw = lerpAngle(currentYaw, targetYaw, 0.1f);
        float newPitch = lerp(currentPitch, targetPitch, 0.1f);

        npc.rotateHead(newYaw, newPitch);

        ticksRemaining--;
    }

    @Override
    public void stop(@NotNull NPC npc)
    {
        isLooking = false;
        ticksRemaining = 0;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public boolean canContinue(@NotNull NPC npc)
    {
        return isLooking;
    }

    /**
     * Picks a new random look direction.
     */
    private void pickNewLookDirection(@NotNull NPC npc)
    {
        Location currentLoc = npc.getLocation();
        float currentYaw = currentLoc.getYaw();
        float currentPitch = currentLoc.getPitch();

        targetYaw = currentYaw + (ThreadLocalRandom.current().nextFloat() * 2 - 1) * YAW_CHANGE_MAX;
        targetPitch = Math.max(-45, Math.min(45, currentPitch + (ThreadLocalRandom.current().nextFloat() * 2 - 1) * PITCH_CHANGE_MAX));
    }

    /**
     * Linearly interpolates between two angles, handling wraparound.
     */
    private float lerpAngle(float from, float to, float alpha)
    {
        float diff = to - from;
        while(diff < -180)
            diff += 360;
        while(diff > 180)
            diff -= 360;
        return from + diff * alpha;
    }

    /**
     * Linearly interpolates between two values.
     */
    private float lerp(float from, float to, float alpha)
    {
        return from + (to - from) * alpha;
    }
}
