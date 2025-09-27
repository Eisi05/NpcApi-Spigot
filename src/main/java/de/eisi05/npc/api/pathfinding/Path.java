package de.eisi05.npc.api.pathfinding;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


/**
 * Represents a path in the world, providing both {@link Location} and {@link Vector} representations
 * of the path's waypoints. The lists returned are unmodifiable to ensure immutability.
 */
public class Path
{
    private final List<Vector> vectors;
    private final List<Location> locations;

    /**
     * Constructs a Path from a list of Bukkit {@link Location} objects.
     * Converts each location to a {@link Vector} for easy mathematical manipulation.
     *
     * @param nodes the ordered list of {@link Location} waypoints
     */
    public Path(@NotNull List<Location> nodes)
    {
        this.locations = Collections.unmodifiableList(nodes);
        this.vectors = nodes.stream().map(Location::toVector).toList();
    }

    /**
     * Constructs a Path from a list of {@link Vector} waypoints in the given world.
     * Converts each vector to a {@link Location} for compatibility with Bukkit APIs.
     *
     * @param nodes the ordered list of {@link Vector} waypoints
     * @param world the Bukkit {@link World} where the locations reside
     */
    public Path(@NotNull List<Vector> nodes, @NotNull World world)
    {
        this.vectors = Collections.unmodifiableList(nodes);
        this.locations = nodes.stream().map(vector -> new Location(world, vector.getX(), vector.getY(), vector.getZ())).toList();
    }

    /**
     * Returns the path as an unmodifiable list of {@link Location} objects.
     *
     * @return an unmodifiable list of Bukkit locations representing the path
     */
    public List<Location> asLocations()
    {
        return locations;
    }

    /**
     * Returns the path as an unmodifiable list of {@link Vector} objects.
     *
     * @return an unmodifiable list of vectors representing the path
     */
    public List<Vector> asVectors()
    {
        return vectors;
    }

    @Override
    public String toString()
    {
        Vector start = vectors.get(0);
        Vector end = vectors.get(vectors.size() - 1);

        return String.format("Start: [%.2f, %.2f, %.2f] -> End: [%.2f, %.2f, %.2f]",
                start.getX(), start.getY(), start.getZ(),
                end.getX(), end.getY(), end.getZ());
    }
}
