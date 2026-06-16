package de.eisi05.npc.api.wrapper.objects;

import com.mojang.authlib.GameProfile;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.server.network.ServerGamePacketListenerImpl")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.server.network.PlayerConnection")
public class WrappedConnection extends Wrapper
{
    WrappedConnection(Object handle)
    {
        super(handle);
    }

    WrappedConnection(@NotNull WrappedMinecraftServer server, @NotNull WrappedNetworkManager networkManager, @NotNull WrappedServerPlayer player,
            @NotNull CommonListenerCookie commonListenerCookie)
    {
        super(createInstance(WrappedConnection.class, server, networkManager, player, commonListenerCookie));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "sendPacket")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "b")
    public void sendPacket(@NotNull PacketWrapper packet)
    {
        invokeWrappedMethod(packet);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "connection")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "e")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "c")
    public @NotNull WrappedNetworkManager networkManager()
    {
        return new WrappedNetworkManager(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.network.Connection")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.NetworkManager")
    public static class WrappedNetworkManager extends Wrapper
    {
        private WrappedNetworkManager(Object handle)
        {
            super(handle);
        }

        public static @NotNull WrappedNetworkManager create(@NotNull PacketFlow packetFlow)
        {
            return new WrappedNetworkManager(createInstance(WrappedNetworkManager.class, packetFlow));
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "channel")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "k")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "n")
        public @NotNull Channel channel()
        {
            return getWrappedFieldValue();
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "channel")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "k")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_9), path = "n")
        public void setChannel(@Nullable Channel channel)
        {
            setWrappedFieldValue(channel);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.network.protocol.PacketFlow")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.EnumProtocolDirection")
        public enum PacketFlow implements EnumWrapper
        {
            @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "SERVERBOUND")
            @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
            SERVERBOUND,

            @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "CLIENTBOUND")
            @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "b")
            CLIENTBOUND;

            @Override
            public @NotNull Object getHandle()
            {
                return cast(this);
            }
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_2), path = "net.minecraft.server.network.CommonListenerCookie")
    public static class CommonListenerCookie extends Wrapper
    {
        public CommonListenerCookie(@NotNull GameProfile profile, int latency, boolean transferred)
        {
            super(create(profile, latency, transferred));
        }

        private static @NotNull Object create(@NotNull GameProfile profile, int latency, boolean transferred)
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_20_6))
                return createInstance(CommonListenerCookie.class, profile, latency, WrappedClientInformation.createDefault());
            else
                return createInstance(CommonListenerCookie.class, profile, latency, WrappedClientInformation.createDefault(), transferred);
        }
    }
}
