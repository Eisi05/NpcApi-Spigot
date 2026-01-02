package de.eisi05.npc.api.wrapper.objects;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.SerializableFunction;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * Represents a snapshot of an entity's state that can be serialized and later used to restore the entity. This is particularly useful for NPCs and other custom
 * entities that need to be saved and loaded.
 */
public class WrappedEntitySnapshot implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String type;
    private final byte[] data;
    private final SerializableFunction<? extends Entity, ? extends Entity> entityFunction;

    /**
     * Creates a new entity snapshot with the specified entity type and NBT data.
     *
     * @param type The type of entity this snapshot represents
     * @param data The NBT data containing the entity's properties (can be null)
     */
    public WrappedEntitySnapshot(@NotNull EntityType type, @Nullable WrappedCompoundTag data)
    {
        this.type = type.name();
        this.data = data == null ? null : data.getData();
        this.entityFunction = entity -> entity;
    }

    /**
     * Creates a new entity snapshot with a custom entity transformation function.
     *
     * @param type     The type of entity this snapshot represents
     * @param function A function that transforms the created entity before it's used
     */
    public WrappedEntitySnapshot(@NotNull EntityType type, @NotNull SerializableFunction<? extends Entity, ? extends Entity> function)
    {
        this.type = type.name();
        this.data = null;
        this.entityFunction = function;
    }

    /**
     * Creates a new entity snapshot with default NBT data.
     *
     * @param type The type of entity this snapshot represents
     */
    public WrappedEntitySnapshot(@NotNull EntityType type)
    {
        this(type, (WrappedCompoundTag) null);
    }

    /**
     * Gets the type of entity this snapshot represents.
     *
     * @return The entity type
     */
    public @NotNull EntityType getType()
    {
        return EntityType.valueOf(type);
    }

    /**
     * Gets the NBT data containing the entity's properties.
     *
     * @return The entity's NBT data
     */
    public @NotNull WrappedCompoundTag getData()
    {
        return data == null ? new WrappedCompoundTag() : WrappedCompoundTag.parse(data);
    }


    /**
     * Creates a new entity in the specified world using this snapshot's data.
     *
     * @param world The world to create the entity in
     * @return A wrapped entity instance
     * @throws RuntimeException If the entity could not be created
     */
    @SuppressWarnings("unchecked")
    public @NotNull WrappedEntity<?> create(@NotNull World world)
    {
        WrappedEntity<?> entity;
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_2))
        {
            getData().putString("id", type.toLowerCase());

            entity = new WrappedEntity<>(Reflections.invokeStaticMethod("net.minecraft.world.entity.EntityTypes", "a",
                    getData().getHandle(), Var.getNmsLevel(world), Function.identity()).get());
        }
        else if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_9))
        {
            getData().putString("id", type.toLowerCase());
            entity = new WrappedEntity<>(
                    Reflections.invokeStaticMethod("net.minecraft.world.entity.EntityTypes", "a",
                            getData().getHandle(), Var.getNmsLevel(world), WrappedEntity.SpawnReason.LOAD.getHandle(), Function.identity()).get());
        }
        else if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_11))
            entity = new WrappedEntity<>(
                    Reflections.invokeStaticMethod("net.minecraft.world.entity.EntityTypes", "a", Var.toNmsEntityType(getType()), getData().getHandle(),
                            Var.getNmsLevel(world), WrappedEntity.SpawnReason.LOAD.getHandle(), Function.identity()).get());
        else
            entity = new WrappedEntity<>(
                    Reflections.invokeStaticMethod("net.minecraft.world.entity.EntityTypes", "a", Var.toNmsEntityType(getType()),
                            getData().getHandle(), Var.getNmsLevel(world), WrappedEntity.SpawnReason.LOAD.getHandle(),
                            WrappedEntity.EntityProcessor.NOP).get());

        WrappedEntity<?> finalEntity = entityFunction == null ? entity :
                WrappedEntity.fromEntity(entityFunction.apply(Var.unsafeCast(entity.getBukkitPlayer())), WrappedEntity.class);
        finalEntity.data = getData().toString();
        return finalEntity;
    }


    /**
     * A wrapper for Minecraft's NBT (Named Binary Tag) compound tag system, providing a type-safe way to read and write NBT data.
     */
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.nbt.NBTTagCompound")
    public static class WrappedCompoundTag extends Wrapper
    {
        /**
         * Creates a new WrappedCompoundTag from a native NBT tag handle.
         *
         * @param handle The native NBT tag object
         */
        private WrappedCompoundTag(Object handle)
        {
            super(handle);
        }

        /**
         * Creates a new, empty WrappedCompoundTag.
         */
        private WrappedCompoundTag()
        {
            super(createInstance(WrappedCompoundTag.class));
        }

        /**
         * Parses a JSON string into a WrappedCompoundTag.
         *
         * @param s The JSON string to parse
         * @return A new WrappedCompoundTag containing the parsed data
         * @throws CommandSyntaxException If the JSON is invalid
         */
        public static WrappedCompoundTag parse(@NotNull String s) throws CommandSyntaxException
        {
            try
            {
                String methodName = Versions.isCurrentVersionSmallerThan(Versions.V1_18) ? "parse" : "a";
                return new WrappedCompoundTag(Reflections.invokeStaticMethod("net.minecraft.nbt.MojangsonParser", methodName, s).get());
            }
            catch(RuntimeException e1)
            {
                if(e1.getCause() instanceof InvocationTargetException e2 && e2.getCause() instanceof CommandSyntaxException e3)
                    throw e3;
                throw e1;
            }
        }

        /**
         * Parses a byte array containing NBT data into a WrappedCompoundTag.
         *
         * @param bytes The NBT data as a byte array
         * @return A new WrappedCompoundTag containing the parsed data
         */
        public static WrappedCompoundTag parse(byte[] bytes)
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_20_4))
                return new WrappedCompoundTag(
                        Reflections.invokeStaticMethod("net.minecraft.nbt.NBTCompressedStreamTools", "a", new ByteArrayInputStream(bytes)).get());

            return new WrappedCompoundTag(
                    Reflections.invokeStaticMethod("net.minecraft.nbt.NBTCompressedStreamTools", "a", new ByteArrayInputStream(bytes),
                            NbtAccounter.unlimitedHeap().getHandle()).get());
        }

        /**
         * Stores a string value in this compound tag.
         *
         * @param key   The key to store the value under
         * @param value The string value to store
         */
        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setString")
        public void putString(@NotNull String key, @NotNull String value)
        {
            invokeWrappedMethod(key, value);
        }

        /**
         * Gets a boolean value from this compound tag.
         *
         * @param key The key of the boolean value
         * @return The boolean value, or false if the key doesn't exist
         */
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_11), path = "b")
        @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_4), path = "q")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getBoolean")
        public boolean getBoolean(@NotNull String key)
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_5))
                return invokeWrappedMethod(key);
            return invokeWrappedMethod(key, false);
        }

        private byte[] getData()
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Reflections.invokeStaticMethod("net.minecraft.nbt.NBTCompressedStreamTools", "a", getHandle(), baos);
            return baos.toByteArray();
        }

        @Override
        public String toString()
        {
            return getHandle().toString();
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.nbt.NBTReadLimiter")
    public static class NbtAccounter extends Wrapper
    {
        private NbtAccounter(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "c")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_2, to = Versions.V1_21_9), path = "a")
        public static NbtAccounter unlimitedHeap()
        {
            return new NbtAccounter(invokeStaticWrappedMethod());
        }
    }
}
