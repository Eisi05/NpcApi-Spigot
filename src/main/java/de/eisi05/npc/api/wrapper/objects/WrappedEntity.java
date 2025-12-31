package de.eisi05.npc.api.wrapper.objects;

import com.google.common.collect.ImmutableList;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.packets.CustomPacket;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.entity.Entity")
public class WrappedEntity<T extends Entity> extends Wrapper
{
    WrappedEntity(Object handle)
    {
        super(handle);
    }

    public static <T extends Entity, V extends WrappedEntity<T>> @NotNull V fromEntity(@NotNull T entity, @NotNull Class<V> clazz)
    {
        try
        {
            Constructor<V> constructor = clazz.getDeclaredConstructor(Object.class);
            constructor.setAccessible(true);
            return constructor.newInstance(Reflections.invokeMethod(entity, "getHandle").get());
        }
        catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            return null;
        }
    }

    public static <T extends WrappedEntity<? extends Entity>> boolean is(WrappedEntity<?> entity, Class<T> clazz)
    {
        Mapping[] mappings = clazz.getAnnotationsByType(Mapping.class);

        for(Mapping mapping : mappings)
        {
            if(!Versions.containsCurrentVersion(mapping))
                continue;

            return getTargetClass(mapping).equals(entity.getHandle().getClass());
        }

        return false;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "getBukkitEntity")
    public @NotNull T getBukkitPlayer()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setLocation")
    public void moveTo(@NotNull Location location)
    {
        invokeWrappedMethod(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "aD")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "aC")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "au")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "ar")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "au")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "ar")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "ap")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "an")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "al")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20), path = "aj")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "al")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_19_1), path = "ai")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getDataWatcher")
    public @NotNull WrappedEntityData getEntityData()
    {
        return new WrappedEntityData(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "aS")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "aR")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "u")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "q")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "p")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_4), path = "r")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_19_3), path = "au")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "at")
    public void setPassengers(WrappedEntity<?>... entities)
    {
        setWrappedFieldValue(ImmutableList.copyOf(Arrays.stream(entities).map(wrappedEntity -> wrappedEntity.handle).toList()));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "ao")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "an")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "ai")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "cU")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "cN")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "cK")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "cJ")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "cH")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_4), path = "cG")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "cH")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_1), path = "cC")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19), path = "cD")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_18_2), path = "cA")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getWorld")
    public @NotNull Object getServer()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "dl")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "dj")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "di")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20), path = "S")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "T")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_19_1), path = "S")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getPacket")
    public @NotNull PacketWrapper getAddEntityPacket()
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21))
            return new CustomPacket(invokeWrappedMethod());
        return new CustomPacket(invokeWrappedMethod(new WrappedEntityTrackerEntry(this)));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "dj")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "de")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "cV")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "cR")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "cK")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "cH")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "cG")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "cE")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_19_4), path = "cD")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "cz")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_18_2), path = "cw")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getBoundingBox")
    public BoundingBox getBoundingBox()
    {
        return new BoundingBox(invokeWrappedMethod());
    }

    public int getId()
    {
        return getBukkitPlayer().getEntityId();
    }

    public @NotNull World getWorld()
    {
        return getBukkitPlayer().getWorld();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_11), path = "net.minecraft.world.entity.Entity$RemovalReason")
    enum RemovalReason implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_11), path = "b")
        DISCARDED;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.entity.EntitySpawnReason")
    enum SpawnReason implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "r")
        LOAD;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.entity.EntityTypes")
    public static class EntityTypes extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "h")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_6), path = "g")
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "f")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21), path = "d")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_18_2), path = "c")
        public static final Object ARMOR_STAND = getStaticWrappedFieldValue("ARMOR_STAND").orElse(null);

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V1_21_11), path = "bD")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "bA")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "bx")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "bw")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_4), path = "bu")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_2), path = "bv")
        @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "bb")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "aY")
        @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_20_2), path = "aX")
        public static final Object TEXT_DISPLAY = getStaticWrappedFieldValue("TEXT_DISPLAY").orElse(null);

        private EntityTypes()
        {
            super(null);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.phys.AxisAlignedBB")
    public static class BoundingBox extends Wrapper
    {
        private BoundingBox(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "c")
        public double getYSize()
        {
            return invokeWrappedMethod();
        }
    }

    public abstract static class WrappedNameTag<T extends Entity> extends WrappedEntity<T>
    {
        protected WrappedNameTag(Object handle)
        {
            super(handle);
        }

        public abstract WrappedEntityData applyData(WrappedComponent component);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.entity.EntityProcessor")
    static class EntityProcessor extends Wrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        public static final Object NOP = getStaticWrappedFieldValue("NOP").orElse(null);

        private EntityProcessor()
        {
            super(null);
        }
    }
}
