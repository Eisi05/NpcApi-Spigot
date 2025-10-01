package de.eisi05.npc.api.pathfinding;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for calculating paths between locations using A* pathfinding.
 * Provides synchronous and asynchronous methods.
 */
public class PathfindingUtils
{
    /**
     * Asynchronously calculates a path through a list of waypoints.
     * <p>
     * Each segment between consecutive waypoints is calculated in parallel using {@link CompletableFuture}.
     * The returned future completes with a {@link Path} containing the full path, or completes exceptionally
     * if an {@link AStar.InvalidPathException} occurs.
     *
     * @param waypoints             the ordered list of locations to traverse
     * @param maxIterations         the maximum number of iterations the A* algorithm will attempt per segment
     * @param allowDiagonalMovement whether diagonal movement is allowed
     * @return a {@link CompletableFuture} that completes with the calculated {@link Path}
     */
    public static @NotNull CompletableFuture<Path> findPathAsync(@NotNull List<Location> waypoints, int maxIterations, boolean allowDiagonalMovement)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                return findPath(waypoints, maxIterations, allowDiagonalMovement);
            } catch(AStar.InvalidPathException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Synchronously calculates a path through a list of waypoints.
     * <p>
     * Each segment between consecutive waypoints is calculated in parallel internally using {@link CompletableFuture},
     * but this method blocks until all segments are calculated and combined into a single {@link Path}.
     *
     * @param waypoints             the ordered list of locations to traverse
     * @param maxIterations         the maximum number of iterations the A* algorithm will attempt per segment
     * @param allowDiagonalMovement whether diagonal movement is allowed
     * @return the calculated {@link Path} containing all intermediate locations
     * @throws AStar.InvalidPathException if any segment's start or end location is invalid/unwalkable
     */
    public static @NotNull Path findPath(@NotNull List<Location> waypoints, int maxIterations, boolean allowDiagonalMovement)
            throws AStar.InvalidPathException
    {
        List<CompletableFuture<List<Location>>> futures = new ArrayList<>();

        for(int i = 0; i < waypoints.size() - 1; i++)
        {
            final int idx = i;
            futures.add(CompletableFuture.supplyAsync(() ->
            {
                try
                {
                    AStar aStar = new AStar(waypoints.get(idx), waypoints.get(idx + 1), maxIterations, allowDiagonalMovement);
                    return aStar.iterate().stream()
                            .map(tile -> tile.getLocation(aStar.getStart()).add(0.5, 1, 0.5))
                            .toList();
                } catch(AStar.InvalidPathException e)
                {
                    throw new RuntimeException(e);
                }
            }));
        }

        return new Path(futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList(), waypoints);
    }
}
