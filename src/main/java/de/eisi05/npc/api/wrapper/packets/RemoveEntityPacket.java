package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutEntityDestroy")
public class RemoveEntityPacket extends PacketWrapper
{
    public RemoveEntityPacket(int entityId)
    {
        super(RemoveEntityPacket.class, entityId);
    }
}
