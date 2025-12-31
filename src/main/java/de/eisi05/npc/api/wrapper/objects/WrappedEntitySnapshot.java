package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

public class WrappedEntitySnapshot implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String type;
    private final byte[] data;

    public WrappedEntitySnapshot(@NotNull EntityType type, @Nullable WrappedCompoundTag data)
    {
        this.type = type.name();
        this.data = data == null ? null : data.getData();
    }

    public WrappedEntitySnapshot(@NotNull EntityType type)
    {
        this(type, null);
    }

    public @NotNull EntityType getType()
    {
        return EntityType.valueOf(type);
    }

    public @NotNull WrappedCompoundTag getData()
    {
        return data == null ? new WrappedCompoundTag() : WrappedCompoundTag.parse(data);
    }

    public @NotNull WrappedEntity<?> create(@NotNull World world)
    {
        return new WrappedEntity<>(
                Reflections.invokeStaticMethod("net.minecraft.world.entity.EntityTypes", "a", Var.toNmsEntityType(getType()),
                        getData().getHandle(), Var.getNmsLevel(world), WrappedEntity.SpawnReason.LOAD.getHandle(), WrappedEntity.EntityProcessor.NOP).get());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.nbt.NBTTagCompound")
    public static class WrappedCompoundTag extends Wrapper
    {
        private WrappedCompoundTag(Object handle)
        {
            super(handle);
        }

        private WrappedCompoundTag()
        {
            super(createInstance(WrappedCompoundTag.class));
        }

        public static WrappedCompoundTag parse(@NotNull String s)
        {
            return new WrappedCompoundTag(Reflections.invokeStaticMethod("net.minecraft.nbt.MojangsonParser", "a", s).get());
        }

        public static WrappedCompoundTag parse(byte[] bytes)
        {
            return new WrappedCompoundTag(
                    Reflections.invokeStaticMethod("net.minecraft.nbt.NBTCompressedStreamTools", "a", new ByteArrayInputStream(bytes),
                            NbtAccounter.unlimitedHeap().getHandle()).get());
        }

        private byte[] getData()
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Reflections.invokeStaticMethod("net.minecraft.nbt.NBTCompressedStreamTools", "a", getHandle(), baos);
            return baos.toByteArray();
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.nbt.NBTReadLimiter")
    public static class NbtAccounter extends Wrapper
    {
        private NbtAccounter(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "c")
        public static NbtAccounter unlimitedHeap()
        {
            return new NbtAccounter(invokeStaticWrappedMethod());
        }
    }
}
