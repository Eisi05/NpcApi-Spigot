package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_1), path = "net.minecraft.network.protocol.game" +
        ".ClientboundPlayerInfoUpdatePacket")
public class PlayerInfoUpdatePacket extends PacketWrapper
{
    public PlayerInfoUpdatePacket(@NotNull Action action, @NotNull WrappedServerPlayer serverPlayer)
    {
        super(PlayerInfoUpdatePacket.class, action, serverPlayer);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.protocol.game" +
            ".ClientboundPlayerInfoUpdatePacket$Action")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game" +
            ".ClientboundPlayerInfoUpdatePacket$a")
    public enum Action implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "ADD_PLAYER")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
        ADD_PLAYER,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "INITIALIZE_CHAT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "b")
        INITIALIZE_CHAT,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_GAME_MODE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "c")
        UPDATE_GAME_MODE,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_LISTED")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "d")
        UPDATE_LISTED,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_LATENCY")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "e")
        UPDATE_LATENCY,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_DISPLAY_NAME")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "f")
        UPDATE_DISPLAY_NAME,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_LIST_ORDER")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_11), path = "g")
        UPDATE_LIST_ORDER,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "UPDATE_HAT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_4, to = Versions.V1_21_11), path = "h")
        UPDATE_HAT;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
