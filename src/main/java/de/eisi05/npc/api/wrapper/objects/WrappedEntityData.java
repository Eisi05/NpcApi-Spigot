package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.syncher.SynchedEntityData")
@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.syncher.DataWatcher")
public class WrappedEntityData extends Wrapper
{
    WrappedEntityData(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "set")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "a")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_20_4), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "set")
    public <T> void set(@NotNull EntityDataAccessor<T> accessor, @Nullable T value)
    {
        invokeWrappedMethod(accessor, value);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "get")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "get")
    public <T> @Nullable T get(@NotNull EntityDataAccessor<T> accessor)
    {
        return invokeWrappedMethod(accessor);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "packDirty")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "b")
    public @NotNull List<?> packDirty()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getNonDefaultValues")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "c")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getAll")
    public List<?> getAll()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.syncher.EntityDataAccessor")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.syncher.DataWatcherObject")
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

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.syncher.EntityDataSerializer")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.syncher.DataWatcherSerializer")
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

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.network.syncher.EntityDataSerializers")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.syncher.DataWatcherRegistry")
    public static class EntityDataSerializers extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "BYTE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<>(getStaticWrappedFieldValue("BYTE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "INT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "b")
        public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<>(getStaticWrappedFieldValue("INT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "LONG")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "c")
        public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<>(getStaticWrappedFieldValue("LONG").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "FLOAT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "d")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "c")
        public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<>(getStaticWrappedFieldValue("FLOAT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "STRING")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "e")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "d")
        public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<>(getStaticWrappedFieldValue("STRING").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "COMPONENT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "f")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "e")
        public static final EntityDataSerializer<?> CHAT_COMPONENT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("CHAT_COMPONENT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_COMPONENT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "g")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "f")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_CHAT_COMPONENT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_CHAT_COMPONENT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "ITEM_STACK")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_11), path = "h")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "g")
        public static final EntityDataSerializer<?> ITEM_STACK = new EntityDataSerializer<>(getStaticWrappedFieldValue("ITEM_STACK").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "BLOCK_STATE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_11), path = "i")
        public static final EntityDataSerializer<?> BLOCK_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("BLOCK_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_BLOCK_STATE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_11), path = "j")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "i")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "h")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_BLOCK_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_BLOCK_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "BOOLEAN")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_11), path = "k")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "j")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "i")
        public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<>(getStaticWrappedFieldValue("BOOLEAN").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "PARTICLE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_11), path = "l")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "k")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "j")
        public static final EntityDataSerializer<?> PARTICLE_PARAM = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PARTICLE_PARAM").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "PARTICLES")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "m")
        public static final EntityDataSerializer<List<?>> PARTICLE_PARAM_LIST = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("PARTICLE_PARAM_LIST").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "ROTATIONS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "m")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "l")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "k")
        public static final EntityDataSerializer<?> VECTOR = new EntityDataSerializer<>(getStaticWrappedFieldValue("VECTOR").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "BLOCK_POS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "o")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "n")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "m")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "l")
        public static final EntityDataSerializer<?> BLOCK_POSITION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("BLOCK_POSITION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_BLOCK_POS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "p")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "o")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "n")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_1), path = "m")
        public static final EntityDataSerializer<?> OPTIONAL_BLOCK_POSITION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_BLOCK_POSITION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "DIRECTION")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "q")
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

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_LIVING_ENTITY_REFERENCE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_11), path = "r")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_ENTITY_REFERENCE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_ENTITY_REFERENCE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_GLOBAL_POS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "r")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "q")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "p")
        public static final EntityDataSerializer<Optional<?>> OPTIONAL_GLOBAL_POS = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_GLOBAL_POS").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "s")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "r")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "q")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "p")
        public static final EntityDataSerializer<?> NBT_TAG = new EntityDataSerializer<>(getStaticWrappedFieldValue("NBT_TAG").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "VILLAGER_DATA")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6), path = "u")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "t")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "r")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "q")
        public static final EntityDataSerializer<?> VILLAGER_DATA = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("VILLAGER_DATA").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "OPTIONAL_UNSIGNED_INT")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "u")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6), path = "v")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "u")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "s")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "r")
        public static final EntityDataSerializer<OptionalInt> OPTIONAL_INT = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("OPTIONAL_INT").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "POSE")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "v")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_6), path = "w")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "v")
        @Mapping(fixed = @Mapping.Fixed(value = Versions.V1_19_3), path = "u")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "t")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "s")
        public static final EntityDataSerializer<Pose> ENTITY_POSE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("ENTITY_POSE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "VECTOR3")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "J")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "I")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_6), path = "H")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "D")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "A")
        public static final EntityDataSerializer<Vector3f> VECTOR3 = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("VECTOR3").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "QUARTERNION")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "K")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "J")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_6), path = "I")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_4), path = "E")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "B")
        public static final EntityDataSerializer<?> QUARTERNION = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("QUARTERNION").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "RESOLVABLE_PROFILE")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "L")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "K")
        public static final EntityDataSerializer<?> RESOLVABLE_PROFILE = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("RESOLVABLE_PROFILE").orElse(null));

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "HUMANOID_ARM")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "M")
        public static final EntityDataSerializer<?> HAND = new EntityDataSerializer<>(
                getStaticWrappedFieldValue("HAND").orElse(null));

        private EntityDataSerializers()
        {
            super(null);
        }
    }
}
