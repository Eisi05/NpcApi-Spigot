package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutEntityHeadRotation")
public class RotateHeadPacket extends PacketWrapper
{
    public RotateHeadPacket(@NotNull WrappedServerPlayer serverPlayer, byte yHeadRot)
    {
        super(RotateHeadPacket.class, serverPlayer, yHeadRot);
    }
}
