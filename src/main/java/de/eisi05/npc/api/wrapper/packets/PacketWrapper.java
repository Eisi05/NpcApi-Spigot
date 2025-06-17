package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public abstract class PacketWrapper extends Wrapper
{
    protected PacketWrapper(Object handle)
    {
        super(handle);
    }

    public <T extends Wrapper> PacketWrapper(@NotNull Class<T> clazz, Object... args)
    {
        super(createInstance(clazz, args));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.protocol.Packet")
    public static abstract class PacketHolder extends PacketWrapper
    {
        public PacketHolder(Object handle)
        {
            super(handle);
        }

        public static boolean is(@NotNull Object packet, @NotNull Class<? extends PacketHolder> clazz)
        {
            return Arrays.stream(clazz.getAnnotationsByType(Mapping.class))
                    .filter(Versions::containsCurrentVersion)
                    .findFirst()
                    .filter(data -> getTargetClass(data) != null)
                    .map(data -> getTargetClass(data).isAssignableFrom(packet.getClass())).orElse(false);
        }

        public static <T extends PacketHolder> @NotNull T wrap(@NotNull Object packet, @NotNull Class<T> clazz)
        {
            try
            {
                Constructor<T> constructor = clazz.getDeclaredConstructor(Object.class);
                constructor.setAccessible(true);
                return constructor.newInstance(packet);
            } catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
