package de.eisi05.npc.api.utils;

import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
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
        return Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".inventory.CraftItemStack",
                "asNMSCopy", itemStack).get();
    }

    /**
     * Converts a Bukkit {@link EntityType} to its corresponding NMS (Netty Minecraft Server) entity type object.
     * This is a utility method that uses reflection to access the internal NMS classes.
     *
     * @param entityType The Bukkit {@link EntityType} to convert
     * @return The corresponding NMS entity type object
     * @throws NullPointerException if entityType is null
     */
    public static @NotNull Object toNmsEntityType(@NotNull EntityType entityType)
    {
        return Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".entity.CraftEntityType",
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
        return WrappedComponent.fromHandle(
                Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
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
        return (String) Reflections.invokeStaticMethod("org.bukkit.craftbukkit." + Versions.getVersion().getPath() + ".util.CraftChatMessage",
                "fromComponent", component.getHandle()).get();
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

    public static boolean isCarpet(@NotNull Material material)
    {
        return material.name().contains("CARPET");
    }
}
