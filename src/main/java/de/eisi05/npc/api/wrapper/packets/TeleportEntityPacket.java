package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Set;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutEntityTeleport")
public class TeleportEntityPacket extends PacketWrapper
{
    public TeleportEntityPacket(WrappedEntity<?> entity, PositionMoveRotation positionMoveRotation, Set<?> relatives, boolean onGround)
    {
        super(createInstance(entity, positionMoveRotation, relatives, onGround));
    }

    private static Object createInstance(WrappedEntity<?> entity, PositionMoveRotation positionMoveRotation, Set<?> relatives,
            boolean onGround)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_2))
        {
            Location original = entity.getBukkitPlayer().getLocation();
            entity.moveTo(positionMoveRotation.toLocation(original.getWorld()));
            Object instance = createInstance(TeleportEntityPacket.class, entity.getHandle());
            entity.moveTo(original);
            return instance;
        }

        return createInstance(TeleportEntityPacket.class, entity.getId(), positionMoveRotation.getHandle(), relatives, onGround);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_6), path = "net.minecraft.world.entity.PositionMoveRotation")
    public static class PositionMoveRotation extends Wrapper
    {
        private final Vector position;
        private final float yaw;
        private final float pitch;

        public PositionMoveRotation(Vector position, Vector movement, float yaw, float pitch)
        {
            super(Versions.isCurrentVersionSmallerThan(Versions.V1_21_2) ? null :
                    createInstance(PositionMoveRotation.class, Vec3D.fromVector(position).getHandle(), Vec3D.fromVector(movement).getHandle(), yaw,
                            pitch));

            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        private Location toLocation(World world)
        {
            return new Location(world, position.getX(), position.getY(), position.getZ(), yaw, pitch);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.world.phys.Vec3D")
    private static class Vec3D extends Wrapper
    {
        private Vec3D(double x, double y, double z)
        {
            super(createInstance(Vec3D.class, x, y, z));
        }

        private static Vec3D fromVector(Vector vector)
        {
            return new Vec3D(vector.getX(), vector.getY(), vector.getZ());
        }
    }
}
