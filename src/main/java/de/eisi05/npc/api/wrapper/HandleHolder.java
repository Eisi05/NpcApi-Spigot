package de.eisi05.npc.api.wrapper;

import org.jetbrains.annotations.NotNull;

/**
 * The {@link HandleHolder} interface defines a contract for classes that wrap
 * an underlying "handle" object, typically a Net Minecraft Server (NMS) object.
 * This interface allows for a standardized way to access the NMS instance
 * that a wrapper object represents.
 */
public interface HandleHolder
{
    /**
     * Retrieves the underlying "handle" object.
     * This method is used to access the NMS (Net Minecraft Server) instance
     * that this object wraps.
     *
     * @return The underlying handle object. Must not be {@code null}.
     */
    @NotNull Object getHandle();
}
