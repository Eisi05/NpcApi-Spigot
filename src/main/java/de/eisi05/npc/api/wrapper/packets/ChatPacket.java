package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "net.minecraft.network.protocol.game.PacketPlayOutChat")
public class ChatPacket extends PacketWrapper
{
    private static final UUID NULL_UUID = new UUID(0, 0);

    public ChatPacket(@NotNull WrappedComponent component, @NotNull ChatType type)
    {
        super(ChatPacket.class, component, type, NULL_UUID);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "net.minecraft.network.chat.ChatMessageType")
    public enum ChatType implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "a")
        CHAT,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "b")
        SYSTEM,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "c")
        GAME_INFO;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
