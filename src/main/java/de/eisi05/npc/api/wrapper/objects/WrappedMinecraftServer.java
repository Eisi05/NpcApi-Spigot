package de.eisi05.npc.api.wrapper.objects;

import com.mojang.brigadier.CommandDispatcher;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.server.MinecraftServer")
public class WrappedMinecraftServer extends Wrapper
{
    public static final WrappedMinecraftServer INSTANCE = new WrappedMinecraftServer(Reflections.invokeMethod(Bukkit.getServer(), "getServer").get());

    private WrappedMinecraftServer(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "vanillaCommandDispatcher")
    public @NotNull WrappedMinecraftServer commands()
    {
        return new WrappedMinecraftServer(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_6), path = "ba")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "bc")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "aZ")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "aU")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "aV")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_4), path = "aX")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "aW")
    public Object registryAccess()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "a")
    @SuppressWarnings("unchecked")
    public @NotNull CommandDispatcher<Object> getCommandDispatcher()
    {
        return (CommandDispatcher<Object>) Reflections.invokeMethod(commands().getHandle(), getPath()).get();
    }
}
