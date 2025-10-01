package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.commands.CommandListenerWrapper")
public class WrappedCommandSourceStack extends Wrapper
{
    public WrappedCommandSourceStack(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "getBukkitSender")
    public CommandSender getBukkitSender()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
    public @NotNull WrappedCommandSourceStack withMaximumPermission()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod(4));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
    public @NotNull WrappedCommandSourceStack withSuppressedOutput()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod());
    }
}
