package de.eisi05.npc.api.wrapper.enums;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.world.EnumHand")
public enum InteractionHand implements Wrapper.EnumWrapper
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
    MAIN_HAND,

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "b")
    OFF_HAND;

    @Override
    public @NotNull Object getHandle()
    {
        return cast(this);
    }
}
