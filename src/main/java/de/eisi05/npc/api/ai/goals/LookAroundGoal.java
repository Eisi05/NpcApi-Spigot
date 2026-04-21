package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.wrapper.enums.Pose;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A goal that makes the NPC look around randomly. This can serve as a "wait" goal or an idle behavior when no other goals are active.
 */
public class LookAroundGoal extends Goal
{
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MIN_DURATION = 20; // 1 second
    private static final int DEFAULT_MAX_DURATION = 80; // 4 seconds
    private static final float YAW_CHANGE_MAX = 120f;
    private static final float PITCH_CHANGE_MAX = 40f;
    private static final double ENTITY_LOOK_RANGE = 8.0;
    private static final double ENTITY_LOOK_CHANCE = 0.15;
    private static final double STARING_CHANCE = 0.2;

    private final int minDuration;
    private final int maxDuration;

    private transient int ticksRemaining;
    private transient float targetYaw;
    private transient float targetPitch;
    private transient boolean isLooking;
    private transient float rotationSpeed;

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
        super(Priority.LOW);
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
        rotationSpeed = 0.05f + ThreadLocalRandom.current().nextFloat() * 0.15f;
        pickNewLookDirection(npc);
    }

    @Override
    public void tick(@NotNull NPC npc)
    {
        if(ticksRemaining <= 0)
        {
            decideNextAction(npc);
            ticksRemaining = minDuration + ThreadLocalRandom.current().nextInt(maxDuration - minDuration);
        }

        Location currentLoc = npc.getLocation();
        float currentYaw = currentLoc.getYaw();
        float currentPitch = currentLoc.getPitch();

        float newYaw = lerpAngle(currentYaw, targetYaw, rotationSpeed);
        float newPitch = lerp(currentPitch, targetPitch, rotationSpeed);

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
    public boolean canContinue(@NotNull NPC npc)
    {
        return isLooking;
    }

    /**
     * Decides the next action: look around randomly, stare at current direction, or look at a nearby entity.
     */
    private void decideNextAction(@NotNull NPC npc)
    {
        if(ThreadLocalRandom.current().nextDouble() < ENTITY_LOOK_CHANCE)
        {
            LivingEntity nearbyEntity = findNearbyEntity(npc);
            if(nearbyEntity != null)
            {
                lookAtEntity(npc, nearbyEntity);
                rotationSpeed = 0.1f + ThreadLocalRandom.current().nextFloat() * 0.1f;
                return;
            }
        }

        if(ThreadLocalRandom.current().nextDouble() < STARING_CHANCE)
            rotationSpeed = 0.02f + ThreadLocalRandom.current().nextFloat() * 0.03f;
        else
        {
            pickNewLookDirection(npc);
            rotationSpeed = 0.05f + ThreadLocalRandom.current().nextFloat() * 0.15f;
        }
    }

    /**
     * Finds a nearby living entity to look at.
     */
    private LivingEntity findNearbyEntity(@NotNull NPC npc)
    {
        Location npcLoc = npc.getLocation();
        for(Entity entity : npcLoc.getWorld().getNearbyEntities(npcLoc, ENTITY_LOOK_RANGE, ENTITY_LOOK_RANGE, ENTITY_LOOK_RANGE))
        {
            if(entity instanceof LivingEntity livingEntity && !entity.equals(npc.entity.getBukkitPlayer()))
                return livingEntity;
        }
        return null;
    }

    /**
     * Sets the target direction to look at an entity.
     */
    private void lookAtEntity(@NotNull NPC npc, @NotNull LivingEntity entity)
    {
        Location npcLoc = npc.getLocation();
        Location targetLoc = entity.getLocation();

        double dx = targetLoc.getX() - npcLoc.getX();

        double eyeHeight = (npc.entity.getBukkitPlayer() instanceof LivingEntity le ? le.getEyeHeight() :
                npc.entity.getBukkitPlayer().getHeight()) - (Pose.fromBukkit(npc.getOption(NpcOption.POSE)) == Pose.SITTING ? 0.625 : 0);

        double dy = (targetLoc.getY() + entity.getEyeHeight()) - (npcLoc.getY() + (eyeHeight * npc.getOption(NpcOption.SCALE)));
        double dz = targetLoc.getZ() - npcLoc.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        targetPitch = (float) Math.toDegrees(-Math.atan2(dy, distanceXZ));
    }

    /**
     * Picks a new random look direction.
     */
    private void pickNewLookDirection(@NotNull NPC npc)
    {
        Location currentLoc = npc.getLocation();
        float currentYaw = currentLoc.getYaw();
        float currentPitch = currentLoc.getPitch();

        float yawMultiplier = ThreadLocalRandom.current().nextDouble() < 0.3 ? 1.5f : 1.0f;
        targetYaw = currentYaw + (ThreadLocalRandom.current().nextFloat() * 2 - 1) * YAW_CHANGE_MAX * yawMultiplier;
        targetPitch = Math.max(-60, Math.min(60, currentPitch + (ThreadLocalRandom.current().nextFloat() * 2 - 1) * PITCH_CHANGE_MAX));
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
