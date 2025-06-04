package de.eisi05.npc.api.wrapper.objects;

import com.mojang.brigadier.CommandDispatcher;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.function.UnaryOperator;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.server.MinecraftServer")
public class WrappedMinecraftServer extends Wrapper
{
    public static final WrappedMinecraftServer INSTANCE = new WrappedMinecraftServer(Reflections.invokeMethod(Bukkit.getServer(), "getServer").get());

    private WrappedMinecraftServer(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "vanillaCommandDispatcher")
    public @NotNull WrappedMinecraftServer commands()
    {
        return new WrappedMinecraftServer(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getDedicatedServerProperties")
    public WrappedServerProperties getProperties()
    {
        return new WrappedServerProperties(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "s")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "r")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "s")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_20_2), path = "u")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_18_2), path = "y")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_18), path = "z")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "x")
    public WrappedServerSettings settings()
    {
        return new WrappedServerSettings(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "options")
    public Object options()
    {
        return getWrappedFieldValue();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "ba")
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

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
    @SuppressWarnings("unchecked")
    public @NotNull CommandDispatcher<Object> getCommandDispatcher()
    {
        return (CommandDispatcher<Object>) Reflections.invokeMethod(commands().getHandle(), getPath()).get();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5),
            path = "net.minecraft.server.dedicated.DedicatedServerProperties")
    public static class WrappedServerProperties extends Wrapper
    {
        private WrappedServerProperties(Object handle)
        {
            super(handle);
        }

        public WrappedServerProperties(Properties properties, Object options)
        {
            super(createInstance(WrappedServerProperties.class, properties, options));
        }

        public static WrappedServerProperties fromHandle(Object handle)
        {
            return new WrappedServerProperties(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "X")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "Y")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "X")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_20_4), path = "W")
        public boolean enforceSecureProfile()
        {
            return getWrappedFieldValue();
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_5), path = "ac")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "ab")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_20_4), path = "Z")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_20), path = "Y")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "X")
        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_18_2), path = "Y")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "X")
        public Properties properties()
        {
            return getWrappedFieldValue();
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5),
            path = "net.minecraft.server.dedicated.DedicatedServerSettings")
    public static class WrappedServerSettings extends Wrapper
    {
        private WrappedServerSettings(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "a")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setProperty")
        public WrappedServerSettings update(UnaryOperator<Object> unaryOperator)
        {
            invokeWrappedMethod(unaryOperator);
            return this;
        }
    }
}
