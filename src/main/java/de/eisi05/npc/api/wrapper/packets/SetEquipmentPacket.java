package de.eisi05.npc.api.wrapper.packets;

import com.mojang.datafixers.util.Pair;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutEntityEquipment")
public class SetEquipmentPacket extends PacketWrapper
{
    public SetEquipmentPacket(int entity, @NotNull List<Pair<?, ?>> list)
    {
        super(SetEquipmentPacket.class, entity, list);
    }
}
