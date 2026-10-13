package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.utils.LocationUtils;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import de.eisi05.npc.api.wrapper.packets.MoveEntityPacket;
import de.eisi05.npc.api.wrapper.packets.RotateHeadPacket;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Base class for goals that follow or attack a {@link LivingEntity}. Resolves "where is the ground under the target" once per target <b>block change</b>
 * instead of every tick.
 * <p>
 * <b>Note:</b> subclasses must use the inherited {@link #target} field and must not declare their own field with that name; a field of the same name would
 * shadow it and leave this class without a target.
 */
public abstract class EntityTargetGoal extends PathfindingGoal
{
    private static final Vector DOWN = new Vector(0, -1, 0);

    protected transient LivingEntity target;

    private transient World lastWorld;
    private transient int lastBlockX;
    private transient int lastBlockY;
    private transient int lastBlockZ;
    private transient Location cachedFloorLocation;
    private transient Location cachedGround;
    private transient boolean groundDirty;

    /**
     * Creates a new goal with the specified priority.
     *
     * @param priority The priority level for this goal
     */
    protected EntityTargetGoal(@NotNull Priority priority)
    {
        super(priority);
    }

    /**
     * Creates a new goal with the same priority and pathfinder as the given goal.
     *
     * @param goal The goal to copy
     */
    protected EntityTargetGoal(@NotNull EntityTargetGoal goal)
    {
        super(goal);
    }

    /**
     * Gets the floor location below the target. The ray trace (or nothing at all, if the target stands on the ground) only runs when the target moved to
     * another block.
     * <p>
     * The returned instance is cached and shared: <b>do not modify it</b>, clone it first.
     *
     * @return the floor location, or null if no target is set
     */
    protected final @Nullable Location getTargetLocation()
    {
        if(target == null)
            return null;

        Location current = target.getLocation();
        World world = current.getWorld();
        int bx = current.getBlockX();
        int by = current.getBlockY();
        int bz = current.getBlockZ();

        if(cachedFloorLocation != null && world == lastWorld && bx == lastBlockX && by == lastBlockY && bz == lastBlockZ)
            return cachedFloorLocation;

        lastWorld = world;
        lastBlockX = bx;
        lastBlockY = by;
        lastBlockZ = bz;
        groundDirty = true;

        if(target.isOnGround())
        {
            cachedFloorLocation = current;
            return cachedFloorLocation;
        }

        RayTraceResult result = world.rayTraceBlocks(current, DOWN, 256, FluidCollisionMode.NEVER, true);
        if(result != null && result.getHitBlock() != null)
            cachedFloorLocation = result.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
        else
            cachedFloorLocation = current;

        return cachedFloorLocation;
    }

    /**
     * Gets a standable location at the target's feet, i.e. {@link #getTargetLocation()} adjusted to a safe height for this NPC. Recomputed only when the
     * target moved to another block.
     * <p>
     * The returned instance is cached and shared: <b>do not modify it</b>, clone it first.
     *
     * @param npc the NPC that wants to walk there
     * @return the ground location, or null if no target is set
     */
    protected final @Nullable Location getTargetGround(@NotNull NPC npc)
    {
        Location floor = getTargetLocation();
        if(floor == null)
            return null;

        if(groundDirty || cachedGround == null)
        {
            Location ground = floor.clone();
            OptionalInt safeY = LocationUtils.findSafeY(ground, npc);
            if(safeY.isPresent())
                ground.setY(safeY.getAsInt());

            cachedGround = ground;
            groundDirty = false;
        }
        return cachedGround;
    }

    /**
     * Turns an NPC towards an entity with as little overhead as possible. Replaces calling {@code npc.lookAtEntity(target, viewer, true)} once per viewer per
     * tick, which for every viewer and every tick
     * <ul>
     *     <li>recomputes the angles,</li>
     *     <li>calls {@code npc.setLocation(...)} (position manager update + "unsaved changes" flag), and</li>
     *     <li>sends two packets - even when nothing changed.</li>
     * </ul>
     * This controller computes the angles <b>once</b> per call and only does anything when the <b>quantised</b> angles (the client only receives them in 1.4°
     * steps) actually changed, or as a periodic refresh. The packets and the location update are then sent once for all viewers. The result is visually
     * identical.
     * <p>
     * Instances are cheap and not thread-safe; keep one per goal (lazily created, goals are deserialized without constructors).
     */
    static final class LookController
    {
        /**
         * Even if nothing changed, resend after this many calls (self-heals if something else rotated the NPC).
         */
        private static final int REFRESH_TICKS = 20;
        private static final int VIEWER_REFRESH_TICKS = 40;

        private int lastYaw = Integer.MIN_VALUE;
        private int lastPitch = Integer.MIN_VALUE;
        private int sinceSend;
        private int viewerAge;
        private int viewerCount = -1;
        private List<Player> viewers = List.of();

        /**
         * Forget the last sent angles, so the next {@link #lookAt} always sends.
         */
        void reset()
        {
            lastYaw = Integer.MIN_VALUE;
            lastPitch = Integer.MIN_VALUE;
            sinceSend = REFRESH_TICKS;
        }

        /**
         * Makes the NPC look at the entity.
         *
         * @param npc    the NPC
         * @param target the entity to look at
         * @return true if rotation packets were sent
         */
        boolean lookAt(@NotNull NPC npc, @NotNull Entity target)
        {
            Location npcLoc = npc.getLocation();
            Location targetLoc = target.getLocation();
            if(npcLoc.getWorld() != targetLoc.getWorld() || npc.entity == null)
                return false;

            double dx = targetLoc.getX() - npcLoc.getX();
            double dy = (targetLoc.getY() + (target instanceof LivingEntity living ? living.getEyeHeight() : target.getHeight())) -
                    (npcLoc.getY() + npc.getEyeHeight(null));
            double dz = targetLoc.getZ() - npcLoc.getZ();

            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

            byte yawByte = (byte) (yaw * 256 / 360);
            byte pitchByte = (byte) (pitch * 256 / 360);

            sinceSend++;
            if(yawByte == lastYaw && pitchByte == lastPitch && sinceSend < REFRESH_TICKS)
                return false;

            lastYaw = yawByte;
            lastPitch = pitchByte;
            sinceSend = 0;

            npc.setLocation(new Location(npcLoc.getWorld(), npcLoc.getX(), npcLoc.getY(), npcLoc.getZ(), yaw, pitch));

            List<Player> current = viewers(npc);
            if(current.isEmpty())
                return true;

            RotateHeadPacket head = new RotateHeadPacket(npc.entity, yawByte);
            MoveEntityPacket.Rot body = new MoveEntityPacket.Rot(npc.entity.getId(), yawByte, pitchByte, npc.getServerPlayer().isOnGround());
            for(Player viewer : current)
            {
                WrappedServerPlayer wrapped = WrappedServerPlayer.fromPlayer(viewer);
                wrapped.sendPacket(head);
                wrapped.sendPacket(body);
            }
            return true;
        }

        /**
         * The NPC's current viewers as players, cached until the viewer count changes (or every couple of seconds).
         *
         * @param npc the NPC
         * @return the online viewers
         */
        @NotNull List<Player> viewers(@NotNull NPC npc)
        {
            int size = npc.getViewers().size();
            if(size != viewerCount || ++viewerAge >= VIEWER_REFRESH_TICKS)
            {
                viewerAge = 0;
                viewerCount = size;
                viewers = npc.getViewers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
            }
            return viewers;
        }
    }
}
