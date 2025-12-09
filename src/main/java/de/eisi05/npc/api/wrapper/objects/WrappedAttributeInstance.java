package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;

import java.util.function.Consumer;

@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11),
        path = "net.minecraft.world.entity.ai.attributes.AttributeModifiable")
public class WrappedAttributeInstance extends Wrapper
{
    WrappedAttributeInstance(Object handle)
    {
        super(handle);
    }

    public WrappedAttributeInstance(Object holder, Consumer<?> consumer)
    {
        super(createInstance(WrappedAttributeInstance.class, holder, consumer));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    public void setBaseValue(double value)
    {
        invokeWrappedMethod(value);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11),
            path = "net.minecraft.world.entity.ai.attributes.GenericAttributes")
    public static class Attributes extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "A")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_9), path = "z")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_5), path = "y")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "t")
        public static final Object SCALE_HOLDER = getStaticWrappedFieldValue("SCALE_HOLDER").orElse(null);

        public Attributes()
        {
            super(null);
        }
    }
}
