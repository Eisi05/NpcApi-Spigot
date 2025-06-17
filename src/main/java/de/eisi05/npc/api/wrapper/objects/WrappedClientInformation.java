package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "net.minecraft.server.level.ClientInformation")
public class WrappedClientInformation extends Wrapper
{
    private WrappedClientInformation(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "a")
    public static @NotNull WrappedClientInformation createDefault()
    {
        return new WrappedClientInformation(invokeStaticWrappedMethod());
    }
}
