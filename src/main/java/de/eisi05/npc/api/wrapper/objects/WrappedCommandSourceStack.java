package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.commands.CommandSourceStack")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.commands.CommandListenerWrapper")
public class WrappedCommandSourceStack extends Wrapper
{
    public WrappedCommandSourceStack(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_2), path = "getBukkitSender")
    public CommandSender getBukkitSender()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "getEntity")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "f")
    public @NotNull WrappedEntity<?> getEntity()
    {
        return new WrappedEntity<>(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withMaximumPermission")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "a")
    public @NotNull WrappedCommandSourceStack withMaximumPermission()
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_11))
            return new WrappedCommandSourceStack(invokeWrappedMethod(4));
        return new WrappedCommandSourceStack(invokeWrappedMethod(WrappedPermissionSet.ALL.getHandle()));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withEntity")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withEntity(WrappedEntity<?> entity)
    {
        try
        {
            Class<?> wrapperClass = getWrappedClass(getClass());
            Method method = wrapperClass.getDeclaredMethod(getPath(), getWrappedClass(WrappedEntity.class));
            method.setAccessible(true);
            method.invoke(getHandle(), entity.getHandle());
            return new WrappedCommandSourceStack(method.invoke(getHandle(), entity.getHandle()));
        }
        catch(Exception e)
        {
            if(NpcApi.config.debug())
                e.printStackTrace();
            return this;
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withSuppressedOutput")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withSuppressedOutput()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod());
    }

    public @NotNull WrappedCommandSourceStack withLocation(@NotNull Location location)
    {
        return withPosition(location.toVector()).withRotation(location.getYaw(), location.getPitch()).withWorld(location.getWorld());
    }

    public @NotNull Location getLocation()
    {
        Vector position = getPosition();
        WrappedVec2F rotation = getRotation();
        return new Location(getWorld(), position.getX(), position.getY(), position.getZ(), rotation.getPitch(), rotation.getYaw());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withPosition")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withPosition(@NotNull Vector vector)
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod(WrappedVec3D.fromVector(vector).getHandle()));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withRotation")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withRotation(float yaw, float pitch)
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod(new WrappedVec2F(yaw, pitch).getHandle()));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withLevel")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withWorld(@NotNull World world)
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod(Var.getNmsLevel(world)));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "isSilent")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "y")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_9), path = "x")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "y")
    public boolean isSilent()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "getPosition")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "e")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "d")
    public @NotNull Vector getPosition()
    {
        return WrappedVec3D.fromHandle(invokeWrappedMethod()).toVector();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "getRotation")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "l")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "k")
    public @NotNull WrappedVec2F getRotation()
    {
        return new WrappedVec2F(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "getLevel")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "e")
    public @NotNull World getWorld()
    {
        return (World) Reflections.invokeMethod(invokeWrappedMethod(), "getWorld").get();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_2), path = "net.minecraft.server.permissions.PermissionSet")
    private static class WrappedPermissionSet extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "NO_PERMISSIONS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "g")
        public static final WrappedPermissionSet NO = new WrappedPermissionSet(getStaticWrappedFieldValue("NO").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "ALL_PERMISSIONS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "h")
        public static final WrappedPermissionSet ALL = new WrappedPermissionSet(getStaticWrappedFieldValue("ALL").orElse(null));

        private WrappedPermissionSet(Object handle)
        {
            super(handle);
        }
    }
}
