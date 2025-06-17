package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedAttributeInstance;

import java.util.List;

@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6),
        path = "net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes")
public class UpdateAttributesPacket extends PacketWrapper
{
    public UpdateAttributesPacket(int id, WrappedAttributeInstance instance)
    {
        super(UpdateAttributesPacket.class, id, List.of(instance.getHandle()));
    }
}
