package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.entity.EnderDragon;

import java.util.Arrays;
import java.util.List;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.world.entity.boss.enderdragon.EnderDragon")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.world.entity.boss.EntityEnderDragon")
public class WrappedEnderDragon extends WrappedEntity<EnderDragon>
{
    WrappedEnderDragon(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "subEntities")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "cF")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "cD")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "cu")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "bS")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_4), path = "cj")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_2), path = "ck")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "co")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "ck")
    public List<Integer> getSubIds()
    {
        Object[] subParts = getWrappedFieldValue();
        return Arrays.stream(subParts).map(object -> new WrappedEntity<>(object).getId()).toList();
    }
}
