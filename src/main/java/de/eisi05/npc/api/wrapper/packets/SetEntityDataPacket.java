package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedEntityData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata")
public class SetEntityDataPacket extends PacketWrapper
{
    public List<?> data;

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_2), path = "")
    private SetEntityDataPacket(int id, @NotNull List<?> data)
    {
        super(SetEntityDataPacket.class, id, data);
        this.data = data;
    }

    public static SetEntityDataPacket create(int id, @NotNull WrappedEntityData data)
    {
        List<?> dataList = data.packDirty();
        return new SetEntityDataPacket(id, dataList == null ? new ArrayList<>() : dataList);
    }

    public static SetEntityDataPacket createNoneDefaults(int id, @NotNull WrappedEntityData data)
    {
        List<?> dataList = data.getAll();
        return new SetEntityDataPacket(id, dataList == null ? new ArrayList<>() : dataList);
    }
}
