package de.eisi05.npc.api.utils;

import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.objects.WrappedCommandSourceStack;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import de.eisi05.npc.api.wrapper.objects.WrappedEntitySnapshot;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link Var} class provides utility methods for converting between Bukkit API objects and their corresponding NMS (Net Minecraft Server) counterparts, as
 * well as handling component conversions and unsafe casting. These methods often rely on reflection due to version-specific NMS paths.
 */
public class Var
{
    /**
     * Converts a Bukkit {@link ItemStack} to its NMS (Net Minecraft Server) equivalent. This method uses reflection to call the appropriate {@code asNMSCopy}
     * method from the CraftBukkit library, which varies by server version.
     *
     * @param itemStack The Bukkit {@link ItemStack} to convert. Must not be {@code null}.
     * @return The NMS {@link Object} representing the {@code ItemStack}.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull Object toNmsItemStack(@NotNull ItemStack itemStack)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".inventory.CraftItemStack",
                    "asNMSCopy", itemStack).get();

        return Reflections.invokeStaticMethod("org.bukkit.craftbukkit.inventory.CraftItemStack", "asNMSCopy", itemStack).get();
    }

    /**
     * Converts a NMS ItemsSack to its Bukkit equivalent {@link ItemStack}. This method uses reflection to call the appropriate {@code asBukkitCopy} method from
     * the CraftBukkit library, which varies by server version.
     *
     * @param itemStack The NMS ItemStack to convert. Must not be {@code null}.
     * @return The bukkit {@link ItemStack}.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull ItemStack fromNmsItemStack(@NotNull Object itemStack)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return (ItemStack) Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".inventory.CraftItemStack",
                    "asBukkitCopy", itemStack).get();

        return (ItemStack) Reflections.invokeStaticMethod("org.bukkit.craftbukkit.inventory.CraftItemStack", "asBukkitCopy", itemStack).get();
    }

    /**
     * Converts a Bukkit {@link EntityType} to its corresponding NMS (Netty Minecraft Server) entity type object. This is a utility method that uses reflection
     * to access the internal NMS classes.
     *
     * @param entityType The Bukkit {@link EntityType} to convert
     * @return The corresponding NMS entity type object
     * @throws NullPointerException if entityType is null
     */
    public static @NotNull Object toNmsEntityType(@NotNull EntityType entityType)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".entity.CraftEntityType",
                    "bukkitToMinecraft", entityType).get();
        return Reflections.invokeStaticMethod("org.bukkit.craftbukkit.entity.CraftEntityType",
                "bukkitToMinecraft", entityType).get();
    }

    /**
     * Retrieves the NMS (Net Minecraft Server) equivalent of a Bukkit {@link World} object. This typically involves calling the {@code getHandle()} method on
     * the Bukkit World object via reflection to access the underlying NMS world instance.
     *
     * @param world The Bukkit {@link World} to convert. Must not be {@code null}.
     * @return The NMS {@link Object} representing the {@code World} (e.g., {@code net.minecraft.server.level.WorldServer}).
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull Object getNmsLevel(@NotNull World world)
    {
        return Reflections.invokeMethod(world, "getHandle").get();
    }

    /**
     * Converts a plain text {@link String} into a {@link WrappedComponent} (NMS chat component equivalent). This method uses reflection to call the appropriate
     * {@code fromStringOrNull} method from CraftBukkit's CraftChatMessage utility, which handles parsing text into components.
     *
     * @param text The plain text {@link String} to convert. Must not be {@code null}.
     * @return A {@link WrappedComponent} representing the given text.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull WrappedComponent fromString(@NotNull String text)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return WrappedComponent.fromHandle(
                    Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
                            "fromStringOrNull", text, true).get());
        return WrappedComponent.fromHandle(
                Reflections.invokeStaticMethod("org.bukkit.craftbukkit.util.CraftChatMessage",
                        "fromStringOrNull", text, true).get());
    }

    /**
     * Converts a {@link WrappedComponent} (NMS chat component equivalent) back into a plain text {@link String}. This method uses reflection to call the
     * appropriate {@code fromComponent} method from CraftBukkit's CraftChatMessage utility.
     *
     * @param component The {@link WrappedComponent} to convert to a string. Must not be {@code null}.
     * @return A plain text {@link String} representation of the component.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull String toString(@NotNull WrappedComponent component)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return (String) Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
                    "fromComponent", component.getHandle()).get();
        return (String) Reflections.invokeStaticMethod("org.bukkit.craftbukkit.util.CraftChatMessage",
                "fromComponent", component.getHandle()).get();
    }

    /**
     * Converts a json text into a {@link WrappedComponent} (NMS chat component equivalent). This method uses reflection to call the appropriate
     * {@code fromStringOrNull} method from CraftBukkit's CraftChatMessage utility, which handles parsing text into components.
     *
     * @param json The json text to convert. Must not be {@code null}.
     * @return A {@link WrappedComponent} representing the given text.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull WrappedComponent fromJson(@NotNull String json)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return WrappedComponent.fromHandle(
                    Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
                            "fromJSONOrNull", json).get());
        return WrappedComponent.fromHandle(
                Reflections.invokeStaticMethod("org.bukkit.craftbukkit.util.CraftChatMessage",
                        "fromJSONOrNull", json).get());
    }

    /**
     * Converts a {@link WrappedComponent} (NMS chat component equivalent) back into json format. This method uses reflection to call the appropriate
     * {@code fromComponent} method from CraftBukkit's CraftChatMessage utility.
     *
     * @param component The {@link WrappedComponent} to convert to json. Must not be {@code null}.
     * @return A json representation of the component.
     * @throws java.util.NoSuchElementException If the reflection method is not found or cannot be invoked.
     */
    public static @NotNull String toJson(@NotNull WrappedComponent component)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return (String) Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
                    "toJSON", component.getHandle()).get();
        return (String) Reflections.invokeStaticMethod("org.bukkit.craftbukkit.util.CraftChatMessage",
                "toJSON", component.getHandle()).get();
    }

    /**
     * Resolves and updates a {@link WrappedComponent} in the context of a specific entity and command source stack. This method handles version differences in
     * NMS chat component utilities, using reflection to interact with Minecraft's internal component resolution mechanisms.
     *
     * @param sourceStack The {@link WrappedCommandSourceStack} providing the context for the resolution. Can be {@code null}.
     * @param component   The {@link WrappedComponent} to update. Must not be {@code null}.
     * @param entity      The {@link WrappedEntity} acting as an entity override during resolution. Can be {@code null}.
     * @param depth       The recursion depth limit for resolving nested components or selectors.
     * @return A new {@link WrappedComponent} with updated and resolved content.
     * @throws java.util.NoSuchElementException If the reflection method or necessary classes are not found or cannot be invoked.
     */
    public static @NotNull WrappedComponent updateForEntity(@Nullable WrappedCommandSourceStack sourceStack,
                                                            @NotNull WrappedComponent component, @Nullable WrappedEntity<?> entity, int depth)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V26_1))
            return WrappedComponent.fromHandle(Reflections.invokeStaticMethod("net.minecraft.network.chat.ChatComponentUtils",
                    "a", sourceStack.getHandle(), component.getHandle(), entity.getHandle(), depth).get());
        return WrappedComponent.fromHandle(Reflections.invokeStaticMethod("net.minecraft.network.chat.ComponentUtils",
                "resolve", new ResolutionContextBuilder().withSource(sourceStack).withEntityOverride(entity).build(), component.getHandle(), depth).get());
    }

    /**
     * Performs an unchecked cast of an object to a specified type. This method can be used to bypass Java's type checking at compile time, but it comes with
     * the risk of {@link ClassCastException} at runtime if the object is not an instance of the target type.
     *
     * @param o   The object to cast. Can be {@code null}.
     * @param <T> The target type to which the object will be cast.
     * @return The object cast to the specified type, or {@code null} if the input object was {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T unsafeCast(@Nullable Object o)
    {
        return (T) o;
    }

    /**
     * Converts a WrappedEntitySnapshot.WrappedCompoundTag containing common entity state booleans into a single byte representing the entity flags for accessor
     * 0.
     * <p>
     * Each bit in the returned byte corresponds to a specific entity state:
     * <ul>
     *     <li>Bit 0 (0x01) - HasVisualFire</li>
     *     <li>Bit 1 (0x02) - IsCrouching</li>
     *     <li>Bit 2 (0x04) - IsRiding</li>
     *     <li>Bit 3 (0x08) - IsSprinting</li>
     *     <li>Bit 4 (0x10) - IsSwimming</li>
     *     <li>Bit 5 (0x20) - IsInvisible</li>
     *     <li>Bit 6 (0x40) - IsGlowing</li>
     *     <li>Bit 7 (0x80) - IsFallFlying</li>
     * </ul>
     *
     * @param nbt The WrappedCompoundTag containing the entity state booleans.
     * @return A byte where each bit represents the corresponding entity state as listed above.
     */
    public static byte nbtToEntityFlags(@NotNull WrappedEntitySnapshot.WrappedCompoundTag nbt)
    {
        byte flags = 0;

        if(nbt.getBoolean("HasVisualFire"))
            flags |= 0x01;

        if(nbt.getBoolean("IsCrouching"))
            flags |= 0x02;

        if(nbt.getBoolean("IsRiding"))
            flags |= 0x04;

        if(nbt.getBoolean("IsSprinting"))
            flags |= 0x08;

        if(nbt.getBoolean("IsSwimming"))
            flags |= 0x10;

        if(nbt.getBoolean("IsInvisible"))
            flags |= 0x20;

        if(nbt.getBoolean("IsGlowing"))
            flags |= 0x40;

        if(nbt.getBoolean("IsFallFlying"))
            flags |= (byte) 0x80;

        return flags;
    }

    /**
     * Extracts the entity flag byte (accessor 0) from the current state of a Bukkit {@link Entity}.
     * <p>
     * This method interprets Bukkit-level entity state as user intent and converts it into the corresponding Minecraft entity flags. It is intended to be used
     * as an internal translation layer when NMS classes must not be exposed to API consumers.
     * <p>
     * <b>Important:</b> This is a best-effort mapping. Bukkit state is not a perfect mirror
     * of the underlying NMS entity flags, so some flags (such as visual fire or swimming) are approximated based on available Bukkit APIs.
     *
     * <p>Flag mapping:</p>
     * <ul>
     *     <li>Bit 0 (0x01) - Visual fire (fire ticks or visual fire)</li>
     *     <li>Bit 1 (0x02) - Sneaking / crouching</li>
     *     <li>Bit 2 (0x04) - Riding / inside vehicle</li>
     *     <li>Bit 3 (0x08) - Sprinting</li>
     *     <li>Bit 4 (0x10) - Swimming</li>
     *     <li>Bit 5 (0x20) - Invisible</li>
     *     <li>Bit 6 (0x40) - Glowing</li>
     *     <li>Bit 7 (0x80) - Fall flying (elytra gliding)</li>
     * </ul>
     *
     * @param entity The Bukkit entity whose state should be converted into entity flags.
     * @return A byte representing the combined entity flags for accessor 0.
     */
    public static byte extractFlagsFromBukkit(@NotNull Entity entity)
    {
        byte flags = 0;

        if(entity.getFireTicks() > 0 || entity.isVisualFire())
            flags |= 0x01;

        if(entity instanceof Player player && player.isSneaking())
            flags |= 0x02;

        if(entity.isInsideVehicle())
            flags |= 0x04;

        if(entity instanceof Player player && player.isSprinting())
            flags |= 0x08;

        if(entity instanceof LivingEntity le && le.isSwimming())
            flags |= 0x10;

        if(entity instanceof LivingEntity le && le.isInvisible())
            flags |= 0x20;

        if(entity.isGlowing())
            flags |= 0x40;

        if(entity instanceof LivingEntity le && le.isGliding())
            flags |= (byte) 0x80;

        return flags;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "net.minecraft.network.chat.ResolutionContext$Builder")
    private static class ResolutionContextBuilder extends Wrapper
    {
        private ResolutionContextBuilder()
        {
            super(createInstance(ResolutionContextBuilder.class));
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withSource")
        public @NotNull ResolutionContextBuilder withSource(@Nullable WrappedCommandSourceStack sourceStack)
        {
            if(sourceStack == null)
                return this;

            invokeWrappedMethod(sourceStack.getHandle());
            return this;
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "withEntityOverride")
        public @NotNull ResolutionContextBuilder withEntityOverride(@Nullable WrappedEntity<?> entityOverride)
        {
            invokeWrappedMethod(entityOverride == null ? null : entityOverride.getHandle());
            return this;
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_2), path = "build")
        public @NotNull Object build()
        {
            return invokeWrappedMethod();
        }
    }
}
