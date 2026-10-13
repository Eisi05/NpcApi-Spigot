package de.eisi05.npc.api.pathfinding.environment;

import de.eisi05.npc.api.pathfinding.algorithm.AStarPathfinder;
import de.eisi05.npc.api.pathfinding.algorithm.BoundingBoxPathfinder;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * {@link BlockView} that reads straight from the live world. <b>Main thread only</b>; used by the small static helpers that run during walking
 * ({@link BoundingBoxPathfinder#resolveGroundSupport}, {@link AStarPathfinder#isPositionValid}).
 */
public final class LiveBlockView extends BlockView
{
    public LiveBlockView(@NotNull World world)
    {
        super(world);
    }

    @Override
    public @NotNull BlockProfile profileAt(int x, int y, int z)
    {
        if(y < getMinY() || y >= getMaxY())
            return BlockProfile.EMPTY;

        World world = getWorld();
        Material type = world.getType(x, y, z);
        if(type.isAir())
            return BlockProfile.EMPTY;

        return BlockProfile.of(world.getBlockData(x, y, z), world, x, y, z);
    }
}
