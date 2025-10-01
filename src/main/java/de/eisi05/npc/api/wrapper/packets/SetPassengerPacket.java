package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.network.protocol.game.PacketPlayOutMount")
public class SetPassengerPacket extends PacketWrapper
{
    public SetPassengerPacket(WrappedEntity<?> entity)
    {
        super(SetPassengerPacket.class, entity.getHandle());
    }
}
