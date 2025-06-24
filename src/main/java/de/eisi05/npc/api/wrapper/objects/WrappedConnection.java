package de.eisi05.npc.api.wrapper.objects;

import com.mojang.authlib.GameProfile;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.ConnectionProtocol;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_6), path = "net.minecraft.server.network.PlayerConnection")
@Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "net.minecraft.network.PlayerConnection")
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

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_20), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "sendPacket")
    public void sendPacket(@NotNull PacketWrapper packet)
    {
        invokeWrappedMethod(packet);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6), path = "e")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_20_4), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_3), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "a")
    public @NotNull WrappedNetworkManager networkManager()
    {
        return new WrappedNetworkManager(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.NetworkManager")
    public static class WrappedNetworkManager extends Wrapper
    {
        private WrappedNetworkManager(Object handle)
        {
            super(handle);
        }

        public static @NotNull WrappedNetworkManager create(@NotNull PacketFlow packetFlow)
        {
            WrappedNetworkManager networkManager = new WrappedNetworkManager(createInstance(WrappedNetworkManager.class, packetFlow));

            if(Versions.getVersion() != Versions.V1_20_2 && Versions.getVersion() != Versions.V1_20_4)
                return networkManager;

            NioSocketChannel socketChannel = new NioSocketChannel();
            socketChannel.attr(AttributeKey.valueOf("serverbound_protocol")).set(ConnectionProtocol.PLAY.codecData(PacketFlow.SERVERBOUND));
            socketChannel.attr(AttributeKey.valueOf("clientbound_protocol")).set(ConnectionProtocol.PLAY.codecData(PacketFlow.SERVERBOUND));

            networkManager.setChannel(socketChannel);

            return networkManager;
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_18_2, to = Versions.V1_20), path = "m")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18), path = "k")
        public @NotNull Channel channel()
        {
            return getWrappedFieldValue();
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_18_2, to = Versions.V1_20), path = "m")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18), path = "k")
        public void setChannel(@Nullable Channel channel)
        {
            setWrappedFieldValue(channel);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6),
                path = "net.minecraft.network.protocol.EnumProtocolDirection")
        public enum PacketFlow implements EnumWrapper
        {
            @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "a")
            SERVERBOUND,

            @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "b")
            CLIENTBOUND;

            @Override
            public @NotNull Object getHandle()
            {
                return cast(this);
            }
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_6), path = "net.minecraft.server.network.CommonListenerCookie")
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
