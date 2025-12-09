package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.phys.Vec2F")

public class WrappedVec2F extends Wrapper
{
    public WrappedVec2F(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "j")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "i")
    public float getYaw()
    {
        return getWrappedFieldValue();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_9), path = "k")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "j")
    public float getPitch()
    {
        return getWrappedFieldValue();
    }
}
