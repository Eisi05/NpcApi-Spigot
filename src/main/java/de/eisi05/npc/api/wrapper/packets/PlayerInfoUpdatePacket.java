package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
        ".ClientboundPlayerInfoUpdatePacket")
@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo")
public class PlayerInfoUpdatePacket extends PacketWrapper
{
    public PlayerInfoUpdatePacket(@NotNull Action action, @NotNull WrappedServerPlayer serverPlayer)
    {
        super(PlayerInfoUpdatePacket.class, action, serverPlayer);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
            ".ClientboundPlayerInfoUpdatePacket$a")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "net.minecraft.network.protocol.game" +
            ".PacketPlayOutPlayerInfo$EnumPlayerInfoAction")
    public enum Action implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
        ADD_PLAYER,

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "b")
        INITIALIZE_CHAT,

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "c")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "b")
        UPDATE_GAME_MODE,

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "d")
        UPDATE_LISTED,

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "e")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "c")
        UPDATE_LATENCY,

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "f")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "d")
        UPDATE_DISPLAY_NAME,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "e")
        REMOVE_PLAYER,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "g")
        UPDATE_LIST_ORDER,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_4, to = Versions.V1_21_5), path = "h")
        UPDATE_HAT;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
