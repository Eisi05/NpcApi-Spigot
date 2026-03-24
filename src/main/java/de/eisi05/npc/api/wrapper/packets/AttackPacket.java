package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.protocol.game.ServerboundAttackPacket")
public class AttackPacket extends PacketWrapper.PacketHolder
{
    protected AttackPacket(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "entityId")
    public int getId()
    {
        return getWrappedFieldValue();
    }
}
