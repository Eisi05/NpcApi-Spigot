package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.commands.CommandListenerWrapper")
public class WrappedCommandSourceStack extends Wrapper
{
    public WrappedCommandSourceStack(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "getBukkitSender")
    public CommandSender getBukkitSender()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
    public @NotNull WrappedCommandSourceStack withMaximumPermission()
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_11))
            return new WrappedCommandSourceStack(invokeWrappedMethod(4));
        return new WrappedCommandSourceStack(invokeWrappedMethod(WrappedPermissionSet.ALL.getHandle()));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
    public @NotNull WrappedCommandSourceStack withSuppressedOutput()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "net.minecraft.server.permissions.PermissionSet")
    private static class WrappedPermissionSet extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "g")
        public static final WrappedPermissionSet NO = new WrappedPermissionSet(getStaticWrappedFieldValue("NO").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "h")
        public static final WrappedPermissionSet ALL = new WrappedPermissionSet(getStaticWrappedFieldValue("NO").orElse(null));

        private WrappedPermissionSet(Object handle)
        {
            super(handle);
        }
    }
}
