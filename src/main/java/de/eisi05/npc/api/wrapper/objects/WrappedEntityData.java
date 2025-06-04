package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.syncher.DataWatcher")
public class WrappedEntityData extends Wrapper
{
    WrappedEntityData(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "a")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_20_4), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "set")
    public <T> void set(@NotNull EntityDataAccessor<T> accessor, @Nullable T value)
    {
        invokeWrappedMethod(accessor, value);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "b")
    public @NotNull List<?> packDirty()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_5), path = "c")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getAll")
    public List<?> getAll()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.syncher.DataWatcherObject")
    public static class EntityDataAccessor<T> extends Wrapper
    {
        private EntityDataAccessor(Object handle)
        {
            super(handle);
        }

        public static <T> @NotNull EntityDataAccessor<T> create(int i, @NotNull Object serializer)
        {
            return createWrappedInstance(EntityDataAccessor.class, i, serializer);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.syncher.DataWatcherSerializer")
    public static class EntityDataSerializer<T> extends Wrapper
    {
        private EntityDataSerializer(Object handle)
        {
            super(handle);
        }

        public @NotNull EntityDataAccessor<T> create(int i)
        {
            return EntityDataAccessor.create(i, this.handle);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.syncher.DataWatcherRegistry")
    public static class EntityDataSerializers extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "a")
        public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<>(getStaticWrappedFieldValue("BYTE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "b")
        public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<>(getStaticWrappedFieldValue("INT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "c")
        public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<>(getStaticWrappedFieldValue("LONG").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "d")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "c")
        public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<>(getStaticWrappedFieldValue("FLOAT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "e")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "d")
        public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<>(getStaticWrappedFieldValue("STRING").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "f")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "e")
        public static final EntityDataSerializer<?> CHAT_COMPONENT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("CHAT_COMPONENT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "g")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "f")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_CHAT_COMPONENT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_CHAT_COMPONENT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_5), path = "h")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "g")
        public static final EntityDataSerializer<?> ITEM_STACK = new EntityDataSerializer<>(getStaticWrappedFieldValue("ITEM_STACK").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_5), path = "i")
        public static final EntityDataSerializer<?> BLOCK_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("BLOCK_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_5), path = "j")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "i")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "h")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_BLOCK_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_BLOCK_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_5), path = "k")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "j")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "i")
        public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<>(getStaticWrappedFieldValue("BOOLEAN").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_5), path = "l")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "k")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "j")
        public static final EntityDataSerializer<?> PARTICLE_PARAM = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PARTICLE_PARAM").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "m")
        public static final EntityDataSerializer<List<?>> PARTICLE_PARAM_LIST = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PARTICLE_PARAM_LIST").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "m")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "l")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "k")
        public static final EntityDataSerializer<?> VECTOR = new EntityDataSerializer<>(getStaticWrappedFieldValue("VECTOR").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "o")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "n")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "m")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "l")
        public static final EntityDataSerializer<?> BLOCK_POSITION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("BLOCK_POSITION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "p")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "o")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "m")
        public static final EntityDataSerializer<?> OPTIONAL_BLOCK_POSITION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_BLOCK_POSITION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "q")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "p")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "o")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "n")
        public static final EntityDataSerializer<?> ENUM_DIRECTION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("ENUM_DIRECTION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "r")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "q")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "p")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "o")
        public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_UUID").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "r")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_ENTITY_REFERENCE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_ENTITY_REFERENCE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "r")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "q")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "p")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_GLOBAL_POS = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_GLOBAL_POS").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "s")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "r")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "q")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "p")
        public static final EntityDataSerializer<?> NBT_TAG = new EntityDataSerializer<>(getStaticWrappedFieldValue("NBT_TAG").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "u")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "t")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "r")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "q")
        public static final EntityDataSerializer<?> VILLAGER_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("VILLAGER_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "v")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "u")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "r")
        public static final EntityDataSerializer<OptionalInt> OPTIONAL_INT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_INT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "w")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "v")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "u")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "s")
        public static final EntityDataSerializer<Pose> ENTITY_POSE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("ENTITY_POSE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_5), path = "x")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "w")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "v")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "u")
        public static final EntityDataSerializer<?> CAT_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("CAT_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "y")
        public static final EntityDataSerializer<?> CHICKEN_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("CHICKEN_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "z")
        public static final EntityDataSerializer<?> COW_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("COW_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "A")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "y")
        public static final EntityDataSerializer<?> WOLF_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("WOLF_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "B")
        public static final EntityDataSerializer<?> WOLF_SOUND_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("WOLF_SOUND_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "C")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "z")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "x")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "w")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "v")
        public static final EntityDataSerializer<?> FROG_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("FROG_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "D")
        public static final EntityDataSerializer<?> PIG_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PIG_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "E")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "A")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "y")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "x")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "w")
        public static final EntityDataSerializer<?> PAINTING_VARIANT_HOLDER = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PAINTING_VARIANT_HOLDER").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "F")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "B")
        public static final EntityDataSerializer<?> ARMADILLO_STATE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("ARMADILLO_STATE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "G")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "C")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "z")
        public static final EntityDataSerializer<?> SNIFFER_STATE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("SNIFFER_STATE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "H")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "D")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "A")
        public static final EntityDataSerializer<?> VECTOR3 = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("VECTOR3").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_5), path = "I")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "E")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "B")
        public static final EntityDataSerializer<?> QUARTERNION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("QUARTERNION").orElse(null));

        private EntityDataSerializers()
        {
            super(null);
        }
    }
}
