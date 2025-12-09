package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.phys.Vec3D")
public class WrappedVec3D extends Wrapper
{
    private WrappedVec3D(Object handle)
    {
        super(handle);
    }

    public WrappedVec3D(double x, double y, double z)
    {
        super(createInstance(WrappedVec3D.class, x, y, z));
    }

    public static @NotNull WrappedVec3D fromVector(@NotNull Vector vector)
    {
        return new WrappedVec3D(vector.getX(), vector.getY(), vector.getZ());
    }

    public static @NotNull WrappedVec3D fromHandle(@NotNull Object handle)
    {
        return new WrappedVec3D(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_6), path = "d")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21), path = "c")
    public double getX()
    {
        return getWrappedFieldValue();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_6), path = "e")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21), path = "d")
    public double getY()
    {
        return getWrappedFieldValue();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "i")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_6), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21), path = "e")
    public double getZ()
    {
        return getWrappedFieldValue();
    }

    public Vector toVector()
    {
        return new Vector(getX(), getY(), getZ());
    }
}
