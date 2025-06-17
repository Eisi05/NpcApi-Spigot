package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedEntityData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutEntityMetadata")
public class SetEntityDataPacket extends PacketWrapper
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "")
    private SetEntityDataPacket(int id, @NotNull WrappedEntityData data, boolean dirty)
    {
        super(SetEntityDataPacket.class, id, data, dirty);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "")
    private SetEntityDataPacket(int id, @NotNull List<?> data)
    {
        super(SetEntityDataPacket.class, id, data);
    }

    public static SetEntityDataPacket create(int id, @NotNull WrappedEntityData data, boolean nonDefaults)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_3))
            return new SetEntityDataPacket(id, data, nonDefaults);
        else
        {
            List<?> dataList = nonDefaults ? data.getAll() : data.packDirty();
            return new SetEntityDataPacket(id, dataList == null ? new ArrayList<>() : dataList);
        }
    }
}
