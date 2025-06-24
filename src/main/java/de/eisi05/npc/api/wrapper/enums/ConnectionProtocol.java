package de.eisi05.npc.api.wrapper.enums;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.objects.WrappedConnection;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.EnumProtocol")
public enum ConnectionProtocol implements Wrapper.EnumWrapper
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "a")
    HANDSHAKE,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "b")
    PLAY,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "c")
    STATUS,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "d")
    LOGIN,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "e")
    CONFIGURATION;

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "b")
    public @NotNull Object codecData(@NotNull WrappedConnection.WrappedNetworkManager.PacketFlow packetFlow)
    {
        return Reflections.invokeMethod(getHandle(), getPath(), packetFlow.getHandle()).get();
    }

    @Override
    public @NotNull Object getHandle()
    {
        return cast(this);
    }
}
