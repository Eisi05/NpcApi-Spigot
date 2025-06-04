package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game.PacketPlayOutEntity")
public class MoveEntityPacket
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
            ".PacketPlayOutEntity$PacketPlayOutEntityLook")
    public static class Rot extends PacketWrapper
    {
        public Rot(int entityId, byte yRot, byte xRot, boolean onGround)
        {
            super(Rot.class, entityId, yRot, xRot, onGround);
        }
    }
}
