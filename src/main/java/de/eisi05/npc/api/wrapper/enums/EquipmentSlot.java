package de.eisi05.npc.api.wrapper.enums;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.world.entity.EnumItemSlot")
public enum EquipmentSlot implements Wrapper.EnumWrapper
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
    MAINHAND,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "b")
    OFFHAND,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "c")
    FEET,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "d")
    LEGS,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "e")
    CHEST,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "f")
    HEAD,

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "g")
    BODY;

    @Override
    public @NotNull Object getHandle()
    {
        return cast(this);
    }
}
