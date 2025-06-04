package de.eisi05.npc.api.utils.exceptions;

import de.eisi05.npc.api.utils.Versions;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * An exception thrown when a specific version of the server is not found or supported
 * for a particular method or class, indicating incompatibility with the current
 * Bukkit/Minecraft version. This is a {@link RuntimeException}, meaning it is
 * an unchecked exception and does not need to be explicitly caught.
 */
public class VersionNotFound extends RuntimeException
{
    /**
     * Constructs a new {@code VersionNotFound} exception with a detail message
     * indicating that the current server version is not found for a specific method.
     *
     * @param method The {@link Method} for which the version was not found. Must not be {@code null}.
     */
    public VersionNotFound(@NotNull Method method)
    {
        super("Version " + Versions.getVersion().name() + " not found for " + method.getName() + " in " + method.getDeclaringClass().getName());
    }

    /**
     * Constructs a new {@code VersionNotFound} exception with a detail message
     * indicating that the current server version is not found for a specific class.
     *
     * @param clazz The {@link Class} for which the version was not found. Must not be {@code null}.
     */
    public VersionNotFound(@NotNull Class<?> clazz)
    {
        super("Version " + Versions.getVersion().name() + " not found for " + clazz.getName());
    }

    /**
     * Constructs a new {@code VersionNotFound} exception with no detail message.
     * This constructor can be used when the specific context of the version not found
     * is not immediately available or required.
     */
    public VersionNotFound()
    {
        super();
    }

    /**
     * Constructs a new {@code VersionNotFound} exception with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method). Must not be {@code null}.
     */
    public VersionNotFound(@NotNull String message)
    {
        super(message);
    }
}
