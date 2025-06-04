package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
        ".ClientboundPlayerInfoRemovePacket")
public class PlayerInfoRemovePacket extends PacketWrapper
{
    public PlayerInfoRemovePacket(@NotNull List<UUID> list)
    {
        super(PlayerInfoRemovePacket.class, list);
    }
}
