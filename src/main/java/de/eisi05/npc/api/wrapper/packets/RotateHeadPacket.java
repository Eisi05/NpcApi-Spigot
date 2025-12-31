package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game" +
                                                                                       ".PacketPlayOutEntityHeadRotation")
public class RotateHeadPacket extends PacketWrapper
{
    public RotateHeadPacket(@NotNull WrappedEntity entity, byte yHeadRot)
    {
        super(RotateHeadPacket.class, entity, yHeadRot);
    }
}
