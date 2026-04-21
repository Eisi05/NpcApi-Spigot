package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.enums.WalkingResult;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.pathfinding.Path;
import de.eisi05.npc.api.pathfinding.PathfindingUtils;
import de.eisi05.npc.api.utils.SerializableConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A goal that makes the NPC walk to a specific location using pathfinding.
 */
public class WalkToLocationGoal extends Goal
{
    static final int RECALCULATION_COOLDOWN = 20;
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_MAX_ITERATIONS = 5000;
    private static final double DEFAULT_SPEED = 0.4;

    private final Path.SerializablePath.SerializableLocation serializableLocation;
    private final double speed;
    private final int maxIterations;
    private final boolean allowDiagonal;
    private final SerializableConsumer<WalkingResult> completionCallback;

    private transient Location targetLocation;
    private transient CompletableFuture<Void> pathfindingFuture;
    private transient Path currentPath;
    private transient boolean isWalking;

    /**
     * Creates a WalkToLocationGoal with a fixed target location.
     *
     * @param targetLocation The location to walk to
     */
    public WalkToLocationGoal(@NotNull Location targetLocation)
    {
        this(targetLocation, DEFAULT_SPEED);
    }

    /**
     * Creates a WalkToLocationGoal with a fixed target location and custom speed.
     *
     * @param targetLocation The location to walk to
     * @param speed          The walking speed (0.1 to 1.0)
     */
    public WalkToLocationGoal(@NotNull Location targetLocation, double speed)
    {
        this(targetLocation, speed, DEFAULT_MAX_ITERATIONS, true, null);
    }

    /**
     * Creates a WalkToLocationGoal with full configuration options.
     *
     * @param targetLocation     The location to walk to
     * @param speed              The walking speed (0.1 to 1.0)
     * @param maxIterations      Maximum iterations for pathfinding
     * @param allowDiagonal      Whether diagonal movement is allowed
     * @param completionCallback Callback called when walking completes
     */
    public WalkToLocationGoal(@NotNull Location targetLocation, double speed, int maxIterations, boolean allowDiagonal,
                              @NotNull SerializableConsumer<WalkingResult> completionCallback)
    {
        super(Priority.MID);
        this.targetLocation = targetLocation.clone();
        this.serializableLocation = new Path.SerializablePath.SerializableLocation(targetLocation);
        this.speed = Math.max(0.1, Math.min(1.0, speed));
        this.maxIterations = maxIterations;
        this.allowDiagonal = allowDiagonal;
        this.completionCallback = completionCallback;
    }

    @Override
    public boolean canUse(@NotNull NPC npc)
    {
        if(isWalking)
            return true;

        if(targetLocation == null || !targetLocation.getWorld().equals(npc.getLocation().getWorld()))
            return false;

        return npc.getLocation().distance(targetLocation) > 1.0;
    }

    @Override
    public void start(@NotNull NPC npc)
    {
        if(targetLocation == null)
            return;

        calculatePath(npc);
    }

    @Override
    public void tick(@NotNull NPC npc)
    {
        // No dynamic target updates needed
    }

    @Override
    public void stop(@NotNull NPC npc)
    {
        if(pathfindingFuture != null)
        {
            pathfindingFuture.cancel(true);
            pathfindingFuture = null;
        }

        List<Player> viewers = npc.getViewers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

        for(Player viewer : viewers)
        {
            if(npc.isWalking(viewer))
                npc.cancelWalking(viewer);
        }

        isWalking = false;
        currentPath = null;
    }

    @Override
    public boolean canContinue(@NotNull NPC npc)
    {
        return isWalking && targetLocation != null && npc.getLocation().distance(targetLocation) > 1.0;
    }

    /**
     * Gets the target location.
     *
     * @return The target location
     */
    public @NotNull Location getTargetLocation()
    {
        return targetLocation.clone();
    }

    private void calculatePath(@NotNull NPC npc)
    {
        Location start = npc.getLocation();
        Location end = targetLocation;

        if(!end.getWorld().equals(start.getWorld()))
            return;

        PathfindingUtils.findPathAsync(List.of(start, end), maxIterations, allowDiagonal, null)
                .thenAcceptAsync(path ->
                {
                    if(path != null)
                    {
                        currentPath = path;
                        startWalking(npc);
                    }
                    else if(completionCallback != null)
                        completionCallback.accept(WalkingResult.CANCELLED);
                }, task -> Bukkit.getScheduler().runTask(NpcApi.plugin, task))
                .exceptionally(e ->
                {
                    Bukkit.getScheduler().runTask(NpcApi.plugin, () ->
                    {
                        if(completionCallback != null)
                            completionCallback.accept(WalkingResult.CANCELLED);
                    });
                    return null;
                });
    }

    private void startWalking(@NotNull NPC npc)
    {
        List<Player> viewers = npc.getViewers().stream()
                .map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull)
                .toList();

        npc.walkTo(currentPath, speed, true, result ->
        {
            isWalking = false;
            if(completionCallback != null)
                completionCallback.accept(result);
        }, viewers);

        isWalking = true;
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        this.targetLocation = serializableLocation.toLocation();
    }
}
