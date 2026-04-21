package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.objects.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.UUID;

/**
 * A goal that makes the NPC follow a target entity. The NPC will maintain a specified distance from the target and pathfind to them if too far.
 */
public class FollowEntityGoal extends Goal
{
    @Serial
    private static final long serialVersionUID = 1L;

    private static final double DEFAULT_FOLLOW_DISTANCE = 3.0;
    private static final double DEFAULT_STOP_DISTANCE = 1.5;
    private static final double DEFAULT_SPEED = 0.4;

    private final double followDistance;
    private final double stopDistance;
    private final double speed;
    private final UUID targetEntityId;

    private transient WalkToLocationGoal currentWalkGoal;
    private transient LivingEntity target;
    private transient int pathRecalculationCooldown;

    /**
     * Creates a FollowEntityGoal with a fixed target entity ID and default distances.
     *
     * @param targetEntityId The UUID of the entity to follow
     */
    public FollowEntityGoal(@NotNull UUID targetEntityId)
    {
        this(targetEntityId, DEFAULT_FOLLOW_DISTANCE, DEFAULT_STOP_DISTANCE, DEFAULT_SPEED);
    }

    /**
     * Creates a FollowEntityGoal with a fixed target entity ID and full configuration.
     *
     * @param targetEntityId The UUID of the entity to follow
     * @param followDistance The distance at which the NPC will start following
     * @param stopDistance   The distance at which the NPC will stop moving
     * @param speed          The walking speed
     */
    public FollowEntityGoal(@NotNull UUID targetEntityId, double followDistance, double stopDistance, double speed)
    {
        super(Priority.HIGH);
        this.targetEntityId = targetEntityId;
        this.followDistance = Math.max(stopDistance + 0.5, followDistance);
        this.stopDistance = Math.max(0.5, stopDistance);
        this.speed = Math.max(0.1, Math.min(1.0, speed));
    }

    @Override
    public boolean canUse(@NotNull NPC npc)
    {
        LivingEntity potentialTarget = target;
        if(potentialTarget == null || !potentialTarget.isValid())
        {
            Entity entity = Bukkit.getEntity(targetEntityId);
            if(!(entity instanceof LivingEntity le))
                return false;
            potentialTarget = le;
        }

        if(!potentialTarget.isValid())
            return false;

        Location npcLoc = npc.getLocation();
        Location targetLoc = potentialTarget.getLocation();

        if(!npcLoc.getWorld().equals(targetLoc.getWorld()))
            return false;

        double distance = npcLoc.distance(targetLoc);
        return distance <= followDistance && distance > stopDistance;
    }

    @Override
    public void start(@NotNull NPC npc)
    {
        Entity entity = Bukkit.getEntity(targetEntityId);

        if(!(entity instanceof LivingEntity le))
            return;

        this.target = le;
        this.pathRecalculationCooldown = 0;
        if(target != null && target.isValid())
        {
            currentWalkGoal = new WalkToLocationGoal(target.getLocation(), speed);
            currentWalkGoal.start(npc);
        }
    }

    @Override
    public void tick(@NotNull NPC npc)
    {
        if(target == null || !target.isValid())
        {
            Entity entity = Bukkit.getEntity(targetEntityId);
            if(entity instanceof LivingEntity newTarget && newTarget != null && newTarget.isValid())
                target = newTarget;
            else
            {
                stop(npc);
                return;
            }
        }

        double distance = npc.getLocation().distance(target.getLocation());
        if(distance <= stopDistance)
        {
            if(currentWalkGoal != null)
            {
                currentWalkGoal.stop(npc);
                currentWalkGoal = null;
            }
            return;
        }

        if(currentWalkGoal == null)
        {
            currentWalkGoal = new WalkToLocationGoal(target.getLocation(), speed);
            currentWalkGoal.start(npc);
            pathRecalculationCooldown = WalkToLocationGoal.RECALCULATION_COOLDOWN;
        }
        else
        {
            Location currentTarget = currentWalkGoal.getTargetLocation();
            boolean shouldRecalculate = currentTarget.distance(target.getLocation()) > 5.0;

            if(!shouldRecalculate && pathRecalculationCooldown <= 0)
            {
                shouldRecalculate = true;
                pathRecalculationCooldown = WalkToLocationGoal.RECALCULATION_COOLDOWN;
            }

            if(shouldRecalculate)
            {
                currentWalkGoal.stop(npc);
                currentWalkGoal = new WalkToLocationGoal(target.getLocation(), speed);
                currentWalkGoal.start(npc);
                pathRecalculationCooldown = WalkToLocationGoal.RECALCULATION_COOLDOWN;
            }
            else
            {
                currentWalkGoal.tick(npc);
                pathRecalculationCooldown--;
            }
        }
    }

    @Override
    public void stop(@NotNull NPC npc)
    {
        if(currentWalkGoal != null)
        {
            currentWalkGoal.stop(npc);
            currentWalkGoal = null;
        }
        target = null;
    }

    @Override
    public boolean canContinue(@NotNull NPC npc)
    {
        if(target == null || !target.isValid())
            return false;

        Location npcLoc = npc.getLocation();
        Location targetLoc = target.getLocation();

        if(!npcLoc.getWorld().equals(targetLoc.getWorld()))
            return false;

        double distance = npcLoc.distance(targetLoc);
        return distance <= followDistance * 2;
    }
}
