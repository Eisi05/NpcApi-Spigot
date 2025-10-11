package de.eisi05.npc.api.objects;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.enums.Result;
import de.eisi05.npc.api.interfaces.NpcClickAction;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.utils.ObjectSaver;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.objects.WrappedComponent;
import de.eisi05.npc.api.wrapper.objects.WrappedEntityData;
import de.eisi05.npc.api.wrapper.objects.WrappedPlayerTeam;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import de.eisi05.npc.api.wrapper.packets.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Non-Player Character (NPC) with location, appearance, options, and interaction logic.
 */
public class NPC extends NpcHolder
{
    final List<Integer> toDeleteEntities = new ArrayList<>();
    private final List<UUID> viewers = new ArrayList<>();
    private final Map<NpcOption<?, ?>, Object> options;
    private Path npcPath;
    WrappedServerPlayer serverPlayer;
    private WrappedComponent name;
    private Location location;
    private NpcClickAction clickEvent;
    private Instant createdAt = Instant.now();

    /**
     * Creates an NPC at the specified location with a random UUID and default name.
     * The default name is an empty component.
     *
     * @param location the location to spawn the NPC. Must not be null.
     */
    public NPC(@NotNull Location location)
    {
        this(location, UUID.randomUUID());
    }

    /**
     * Creates an NPC at the specified location with a random UUID and given name.
     *
     * @param location the location to spawn the NPC. Must not be null.
     * @param name     the display name of the NPC. Must not be null.
     */
    public NPC(@NotNull Location location, @NotNull WrappedComponent name)
    {
        this(location, UUID.randomUUID(), name);
    }

    /**
     * Creates an NPC at the specified location with the given UUID and default name.
     * The default name is an empty component.
     *
     * @param location the location to spawn the NPC. Must not be null.
     * @param uuid     the UUID of the NPC. Must not be null.
     */
    public NPC(@NotNull Location location, @NotNull UUID uuid)
    {
        this(location, uuid, WrappedComponent.create(null));
    }

    /**
     * Creates an NPC at the specified location with the given UUID and name.
     * This is the primary constructor that initializes the NPC's core properties.
     *
     * @param location the location to spawn the NPC. Must not be null.
     * @param uuid     the UUID of the NPC. Must not be null.
     * @param name     the display name of the NPC. Must not be null.
     */
    public NPC(@NotNull Location location, @NotNull UUID uuid, @NotNull WrappedComponent name)
    {
        this.name = name;
        this.location = location;
        this.serverPlayer = WrappedServerPlayer.create(location, uuid, name);

        npcPath = NpcApi.plugin.getDataFolder().toPath().resolve("NPC").resolve(uuid + ".npc");

        this.options = new HashMap<>();
        for(NpcOption<?, ?> value : NpcOption.values())
            setOption(value, Var.unsafeCast(value.getDefaultValue()));

        NpcManager.addNPC(this);
    }

    /**
     * Private constructor used for creating a copy of an NPC.
     *
     * @param location   The new location for the NPC. Must not be null.
     * @param name       The name for the NPC. Must not be null.
     * @param options    The options map for the NPC. Must not be null.
     * @param clickEvent The click event for the NPC. Can be null.
     */
    private NPC(@NotNull Location location, @NotNull WrappedComponent name, @NotNull Map<NpcOption<?, ?>, Object> options,
            @Nullable NpcClickAction clickEvent)
    {
        this(location, UUID.randomUUID(), name);
        this.options.putAll(options);
        this.clickEvent = clickEvent;
    }

    /**
     * Creates a copy of this NPC at a new location.
     * The copied NPC will have a new UUID but will retain the original NPC's name, options, and click event.
     *
     * @param newLocation the location for the copied NPC. Must not be null.
     * @return the new NPC instance. Will not be null.
     */
    public @NotNull NPC copy(@NotNull Location newLocation)
    {
        return new NPC(newLocation, name.copy(), new HashMap<>(options), clickEvent == null ? null : clickEvent.copy());
    }

    /**
     * Checks if this NPC has been saved to a file.
     *
     * @return {@code true} if the NPC's data file exists, {@code false} otherwise.
     */
    public boolean isSaved()
    {
        return Files.exists(npcPath);
    }

    /**
     * Saves the NPC's data to a file.
     * This method serializes the NPC's current state and writes it to a .npc file.
     *
     * @throws IOException if an I/O error occurs during saving.
     */
    @Override
    public void save() throws IOException
    {
        npcPath.toFile().getParentFile().mkdirs();
        new ObjectSaver(npcPath.toFile()).write(SerializedNPC.serializedNPC(this), false);
        super.save();
    }

    /**
     * Gets the underlying server player representation for this NPC.
     *
     * @return the {@link WrappedServerPlayer} instance for this NPC. Will not be null.
     */
    public @NotNull WrappedServerPlayer getServerPlayer()
    {
        return serverPlayer;
    }

    /**
     * Gets the click action associated with this NPC.
     *
     * @return the {@link NpcClickAction} for this NPC, or {@code null} if no action is set.
     */
    public @Nullable NpcClickAction getClickEvent()
    {
        return clickEvent;
    }

    /**
     * Sets the click action for this NPC.
     *
     * @param event the {@link NpcClickAction} to set, or {@code null} to remove the current action.
     * @return this NPC instance for method chaining. Will not be null.
     */
    public @NotNull NPC setClickEvent(@Nullable NpcClickAction event)
    {
        this.clickEvent = event;
        return this;
    }

    /**
     * Checks if the NPC is currently enabled.
     * An enabled NPC is visible and interactable (unless overridden by player permissions).
     *
     * @return {@code true} if the NPC is enabled, {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        return getOption(NpcOption.ENABLED);
    }

    /**
     * Sets the enabled state of the NPC.
     * Changing this state will trigger a reload of the NPC for all viewers.
     *
     * @param enabled {@code true} to enable the NPC, {@code false} to disable it.
     */
    public void setEnabled(boolean enabled)
    {
        setOption(NpcOption.ENABLED, enabled);
        reload();
    }

    /**
     * Checks if this NPC is marked as editable through the {@code NpcPlugin}.
     * <p>
     * The default state is {@code false}.
     *
     * @return {@code true} if the NPC is editable, {@code false} otherwise
     */
    public boolean isEditable()
    {
        return getOption(NpcOption.EDITABLE);
    }

    /**
     * Sets whether this NPC can be edited through the {@code NpcPlugin}.
     * <p>
     * By default, an NPC is <b>not</b> editable ({@code false}).
     *
     * @param editable {@code true} if the NPC should be editable, {@code false} otherwise
     */
    public void setEditable(boolean editable)
    {
        setOption(NpcOption.EDITABLE, editable);
    }

    /**
     * Sets a specific option for this NPC.
     *
     * @param option the {@link NpcOption} to set. Must not be null.
     * @param value  the value for the option. If {@code null}, the option will be removed (reverting to default).
     * @param <T>    the type of the option's value.
     */
    public <T> void setOption(@NotNull NpcOption<T, ?> option, @Nullable T value)
    {
        if(value == null)
            options.remove(option);
        else
            options.put(option, value);

        if(NpcApi.config.autoUpdate())
        {
            option.getPacket(value, this, null).ifPresent(packetWrapper ->
                    viewers.forEach(uuid ->
                    {
                        Player player = Bukkit.getPlayer(uuid);
                        if(player == null)
                            return;

                        WrappedServerPlayer.fromPlayer(player).sendPacket(packetWrapper);
                    }));
        }
    }

    /**
     * Gets the value of a specific option for this NPC.
     * If the option has not been explicitly set, its default value will be returned.
     *
     * @param option the {@link NpcOption} to get. Must not be null.
     * @param <T>    the type of the option's value.
     * @return the value of the option. Will not be null (guaranteed by NpcOption default values).
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull T getOption(@NotNull NpcOption<T, ?> option)
    {
        return (T) options.getOrDefault(option, option.getDefaultValue());
    }

    /**
     * Plays an animation for this NPC, visible to the specified player.
     *
     * @param player    the player who will see the animation. Must not be null.
     * @param animation the {@link AnimatePacket.Animation} to play. Must not be null.
     */
    public void playAnimation(@NotNull Player player, @NotNull AnimatePacket.Animation animation)
    {
        WrappedServerPlayer.fromPlayer(player).sendPacket(AnimatePacket.create(getServerPlayer(), animation));
    }

    /**
     * Reloads the NPC for all current viewers.
     * This typically involves hiding and then re-showing the NPC to apply any changes.
     */
    public void reload()
    {
        final List<UUID> viewers = new ArrayList<>(this.viewers);
        hideNpcFromAllPlayers();
        WrappedPlayerTeam.clear(getServerPlayer().getName());
        viewers.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> showNPCToPlayer(Bukkit.getPlayer(uuid)));
    }

    /**
     * Gets the current location of the NPC.
     *
     * @return the {@link Location} of the NPC. Will not be null.
     */
    public @NotNull Location getLocation()
    {
        return location;
    }

    /**
     * Sets the location of the NPC.
     * This will also update the underlying server player's position.
     *
     * @param location the new {@link Location} for the NPC. Must not be null.
     */
    public void setLocation(@NotNull Location location)
    {
        this.location = location;
        this.serverPlayer.moveTo(location);
    }

    /**
     * Gets the unique identifier (UUID) of this NPC.
     *
     * @return the {@link UUID} of the NPC. Will not be null.
     */
    public @NotNull UUID getUUID()
    {
        return serverPlayer.getUUID();
    }

    /**
     * Gets the display name of this NPC.
     *
     * @return the {@link WrappedComponent} representing the NPC's name. Will not be null.
     */
    public @NotNull WrappedComponent getName()
    {
        return name;
    }

    /**
     * Sets the display name of this NPC.
     * This also updates the name for the underlying server player and its list name.
     *
     * @param name the new {@link WrappedComponent} name for the NPC. Must not be null.
     */
    public void setName(@NotNull WrappedComponent name)
    {
        this.name = name;
        serverPlayer.setListName(WrappedComponent.parseFromLegacy(name.toLegacy(false).replace("\n", "\\n")));

        viewers.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> WrappedServerPlayer.fromPlayer(Bukkit.getPlayer(uuid))
                .sendPacket(SetEntityDataPacket.create(serverPlayer.getNameTag().getId(),
                        serverPlayer.getNameTag().applyData(Versions.isCurrentVersionSmallerThan(Versions.V1_19_4) || isEnabled() ? name :
                                WrappedComponent.parseFromLegacy(NpcApi.DISABLED_MESSAGE_PROVIDER.apply(Bukkit.getPlayer(uuid))).append(name)))));
    }

    /**
     * Gets the timestamp when this NPC was created.
     *
     * @return the {@link Instant} of creation. Will not be null.
     */
    public @NotNull Instant getCreatedAt()
    {
        return createdAt;
    }

    /**
     * Makes the NPC visible to all currently online players.
     * This respects the NPC's enabled state and player permissions.
     */
    public void showNpcToAllPlayers()
    {
        Bukkit.getOnlinePlayers().forEach(this::showNPCToPlayer);
    }

    /**
     * Makes the NPC visible to a specific player.
     * If the NPC is disabled and the player is not an operator, the NPC will not be shown.
     * This method handles sending all necessary packets to display the NPC correctly.
     *
     * @param player the player to show the NPC to. Must not be null.
     */
    public void showNPCToPlayer(@NotNull Player player)
    {
        if(!getOption(NpcOption.ENABLED) && !player.isOp())
            return;

        if(!player.getWorld().getName().equals(serverPlayer.getWorld().getName()))
        {
            hideNpcFromPlayer(player);
            return;
        }

        if(!serverPlayer.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
            return;

        if(!viewers.contains(player.getUniqueId()))
            viewers.add(player.getUniqueId());

        List<PacketWrapper> packets = new ArrayList<>();

        Arrays.stream(NpcOption.values()).filter(NpcOption::loadBefore)
                .forEach(npcOption -> npcOption.getPacket(getOption(npcOption), this, player).ifPresent(packets::add));

        packets.add(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer));
        packets.add(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, serverPlayer));

        if(!Versions.isCurrentVersionSmallerThan(Versions.V1_19_3))
            packets.add(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LISTED, serverPlayer));

        if(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_2))
            packets.add(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER, serverPlayer));

        packets.add(serverPlayer.getAddEntityPacket());

        boolean modified = WrappedPlayerTeam.exists(player, getServerPlayer().getName());
        WrappedPlayerTeam wrappedPlayerTeam = WrappedPlayerTeam.create(player, getServerPlayer().getName());
        wrappedPlayerTeam.setNameTagVisibility(WrappedPlayerTeam.Visibility.NEVER);

        packets.add(SetPlayerTeamPacket.createAddOrModifyPacket(wrappedPlayerTeam, !modified));
        packets.add(SetPlayerTeamPacket.createPlayerPacket(wrappedPlayerTeam, getServerPlayer().getName(), SetPlayerTeamPacket.Action.ADD));

        packets.add(new RotateHeadPacket(serverPlayer, (byte) ((location.getYaw() % 360) * 256 / 360)));
        packets.add(new MoveEntityPacket.Rot(serverPlayer.getId(), (byte) location.getYaw(), (byte) location.getPitch(), serverPlayer.isOnGround()));

        WrappedEntityData data = serverPlayer.getEntityData();
        data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2), Optional.of(WrappedComponent.create("NPC").getHandle()));
        data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(3), false);
        packets.add(SetEntityDataPacket.create(serverPlayer.getId(), data));

        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_4) || !getOption(NpcOption.HIDE_NAMETAG))
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V1_21))
                serverPlayer.getNameTag()
                        .moveTo(getLocation().clone().add(0, (serverPlayer.getBoundingBox().getYSize() * getOption(NpcOption.SCALE)), 0));

            packets.add(serverPlayer.getNameTag().getAddEntityPacket());

            packets.add(SetEntityDataPacket.create(serverPlayer.getNameTag().getId(), serverPlayer.getNameTag().applyData(
                    Versions.isCurrentVersionSmallerThan(Versions.V1_19_4) || isEnabled() ? name :
                            WrappedComponent.parseFromLegacy(NpcApi.DISABLED_MESSAGE_PROVIDER.apply(player))
                                    .append(WrappedComponent.create("\n").append(name)))));

            if(!Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
                packets.add(new SetPassengerPacket(serverPlayer));
        }

        Arrays.stream(NpcOption.values()).filter(npcOption -> !npcOption.equals(NpcOption.ENABLED) && !npcOption.loadBefore())
                .forEach(npcOption -> npcOption.getPacket(getOption(npcOption), this, player).ifPresent(packets::add));

        NpcOption.ENABLED.getPacket(isEnabled(), this, player).ifPresent(packets::add);

        WrappedServerPlayer wrappedServerPlayer = WrappedServerPlayer.fromPlayer(player);
        packets.forEach(wrappedServerPlayer::sendPacket);
    }

    /**
     * Hides the NPC from all currently online players.
     */
    public void hideNpcFromAllPlayers()
    {
        Bukkit.getOnlinePlayers().forEach(this::hideNpcFromPlayer);
    }

    /**
     * Hides the NPC from a specific player.
     * This method sends packets to remove the NPC and its associated entities from the player's view.
     *
     * @param player the player to hide the NPC from. Must not be null.
     */
    public void hideNpcFromPlayer(@NotNull Player player)
    {
        WrappedServerPlayer wrappedServerPlayer = WrappedServerPlayer.fromPlayer(player);
        wrappedServerPlayer.sendPacket(new RemoveEntityPacket(serverPlayer.getId()));
        wrappedServerPlayer.sendPacket(new RemoveEntityPacket(serverPlayer.getNameTag().getId()));

        if(WrappedPlayerTeam.exists(player, getServerPlayer().getName()))
        {
            WrappedPlayerTeam wrappedPlayerTeam = WrappedPlayerTeam.create(player, getServerPlayer().getName());
            wrappedServerPlayer.sendPacket(
                    SetPlayerTeamPacket.createPlayerPacket(wrappedPlayerTeam, getServerPlayer().getName(), SetPlayerTeamPacket.Action.REMOVE));
            wrappedServerPlayer.sendPacket(SetPlayerTeamPacket.createRemovePacket(wrappedPlayerTeam));
        }

        toDeleteEntities.forEach(integer -> wrappedServerPlayer.sendPacket(new RemoveEntityPacket(integer)));

        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_3))
            wrappedServerPlayer.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.REMOVE_PLAYER, serverPlayer));
        else
            wrappedServerPlayer.sendPacket(new PlayerInfoRemovePacket(List.of(getUUID())));

        viewers.remove(player.getUniqueId());
    }

    /**
     * Deletes the NPC.
     * This hides the NPC from all players, removes it from the NPC manager, and deletes its saved data file.
     */
    public void delete() throws IOException
    {
        hideNpcFromAllPlayers();
        NpcManager.removeNPC(this);

        serverPlayer.getBukkitPlayer().remove();
        serverPlayer.remove();
        serverPlayer = null;

        npcPath.toFile().getParentFile().mkdirs();
        Files.deleteIfExists(npcPath);
    }

    /**
     * Makes the NPC look at a specific player.
     * This calculates the required yaw and pitch and sends update packets to the viewing player.
     *
     * @param viewer the player the NPC should look at. Must not be null.
     */
    public void lookAtPlayer(@NotNull Player viewer)
    {
        Location npcLoc = serverPlayer.getBukkitPlayer().getLocation();
        Location playerLoc = viewer.getLocation();

        if(npcLoc.getWorld() != playerLoc.getWorld())
            return;

        double dx = playerLoc.getX() - npcLoc.getX();
        double dy = ((playerLoc.getY() + viewer.getEyeHeight())) -
                ((npcLoc.getY() + serverPlayer.getBukkitPlayer().getEyeHeight() * getOption(NpcOption.SCALE)));
        double dz = playerLoc.getZ() - npcLoc.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, distanceXZ));

        byte yawByte = (byte) (yaw * 256 / 360);
        byte pitchByte = (byte) (pitch * 256 / 360);

        WrappedServerPlayer player = WrappedServerPlayer.fromPlayer(viewer);

        player.sendPacket(new RotateHeadPacket(serverPlayer, yawByte));
        player.sendPacket(new MoveEntityPacket.Rot(serverPlayer.getId(), yawByte, pitchByte, serverPlayer.isOnGround()));
    }

    /**
     * Moves the NPC along a precomputed {@link de.eisi05.npc.api.pathfinding.Path}, simulating walking, jumping, and gravity.
     * The NPC's position and rotation are updated each tick and sent to the specified player(s).
     *
     * @param path               The {@link de.eisi05.npc.api.pathfinding.Path} containing the ordered waypoints the NPC should follow.
     * @param player             The player who should see the NPC move. If null, updates all viewers in the `viewers` set.
     * @param walkSpeed          The walking speed of the NPC (clamped between 0.1 and 1).
     * @param changeRealLocation If true, the NPC's actual server-side location will be updated; otherwise only packets are sent.
     * @param onEnd              A {@link Runnable} to be executed when the NPC reaches the end of the path.
     * @return The {@link BukkitTask} representing the movement task.
     */
    public @NotNull BukkitTask walkTo(@NotNull de.eisi05.npc.api.pathfinding.Path path, @Nullable Player player, double walkSpeed,
            boolean changeRealLocation, @Nullable Consumer<Result> onEnd)
    {
        final double speed = Math.max(Math.min(walkSpeed, 1), 0.1);

        final double gravity = -0.08;
        final double jumpVelocity = 0.5;
        final double terminal = -0.5;
        final double stepHeight = 0.6;

        return new BukkitRunnable()
        {
            final List<Location> pathPoints = path.asLocations();
            int index = 0;
            Vector current = location.toVector();
            double yVel = 0.0;
            float previousYaw = location.getYaw();
            Vector previousMovement = location.getDirection();

            @Override
            public void run()
            {
                if(index >= pathPoints.size())
                {
                    Location last = pathPoints.get(pathPoints.size() - 1);

                    Vector lastVector = last.toVector();
                    Vector lastMovement = lastVector.clone().subtract(current);

                    RotateHeadPacket rotateHeadPacket = new RotateHeadPacket(serverPlayer, (byte) (last.getYaw() * 256 / 360));
                    TeleportEntityPacket teleportEntityPacket = new TeleportEntityPacket(serverPlayer,
                            new TeleportEntityPacket.PositionMoveRotation(lastVector, lastMovement, last.getYaw(), last.getPitch()), Set.of(), true);

                    TeleportEntityPacket teleportNameTagPacket = null;
                    if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
                    {
                        Vector nameTagVector = lastVector.clone()
                                .add(new Vector(0, (serverPlayer.getBoundingBox().getYSize() * getOption(NpcOption.SCALE)), 0));
                        teleportNameTagPacket = new TeleportEntityPacket(serverPlayer.getNameTag(),
                                new TeleportEntityPacket.PositionMoveRotation(nameTagVector, lastMovement, 0, 0), Set.of(), false);
                    }

                    sendNpcMovePackets(player, teleportEntityPacket, rotateHeadPacket, teleportNameTagPacket);

                    if(changeRealLocation)
                    {
                        setLocation(last);
                        if(player != null)
                        {
                            for(UUID uuid : viewers)
                            {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                if(!offlinePlayer.isOnline() || uuid.equals(player.getUniqueId()))
                                    continue;

                                hideNpcFromPlayer(offlinePlayer.getPlayer());
                                showNPCToPlayer(offlinePlayer.getPlayer());
                            }
                        }
                    }

                    if(onEnd != null)
                        onEnd.accept(Result.SUCCESS);

                    cancel();
                    return;
                }

                Vector target = pathPoints.get(index).toVector();
                Vector toTarget = target.clone().subtract(current);

                if(toTarget.lengthSquared() < 0.04 && Math.abs(toTarget.getY()) < 0.2)
                {
                    index++;
                    return;
                }

                Vector horizontal = new Vector(toTarget.getX(), 0, toTarget.getZ());
                Vector horizontalMove = (horizontal.lengthSquared() > 1e-6) ? horizontal.clone().normalize().multiply(speed) : new Vector(0, 0, 0);

                double nextDist = target.clone().subtract(current.clone().add(horizontalMove)).lengthSquared();
                if(nextDist > toTarget.lengthSquared())
                {
                    current = target;
                    index++;
                    return;
                }

                World world = location.getWorld();
                int bx = (int) Math.floor(current.getX());
                int bz = (int) Math.floor(current.getZ());
                int searchStart = (int) Math.floor(current.getY());
                int groundBlockY = Integer.MIN_VALUE;

                for(int y = searchStart; y >= searchStart - 3; y--)
                {
                    Block block = world.getBlockAt(bx, y - 1, bz);
                    if(block.getType().isSolid() && !block.getType().isAir() && !block.isPassable())
                    {
                        groundBlockY = y - 1;
                        break;
                    }
                }
                if(groundBlockY == Integer.MIN_VALUE)
                    groundBlockY = world.getHighestBlockYAt(bx, bz) - 1;
                double groundY = groundBlockY + 1.0;
                boolean onGround = current.getY() <= groundY + 1e-5;

                if(onGround)
                {
                    if(toTarget.getY() > 0 && toTarget.getY() <= stepHeight && horizontal.lengthSquared() > 1e-6)
                    {
                        current = current.clone().add(new Vector(0, Math.min(toTarget.getY(), stepHeight), 0));
                        yVel = 0;
                        onGround = true;
                    }
                    else if(toTarget.getY() > 0.5)
                    {
                        yVel = jumpVelocity;
                        onGround = false;
                    }
                    else
                    {
                        yVel = 0;
                        current = new Vector(current.getX(), groundY, current.getZ());
                    }
                }

                double yDelta = 0;
                if(!onGround)
                {
                    yVel += gravity;
                    if(yVel < terminal)
                        yVel = terminal;
                    yDelta = yVel;

                    if(current.getY() + yDelta <= groundY)
                    {
                        yDelta = groundY - current.getY();
                        yVel = 0;
                        onGround = true;
                    }
                }

                Vector movement = new Vector(horizontalMove.getX(), yDelta, horizontalMove.getZ());
                current = current.clone().add(movement);

                Vector lookDir;
                if(index + 1 < pathPoints.size())
                {
                    Vector currentTarget = pathPoints.get(index).toVector().clone();
                    Vector nextTarget = pathPoints.get(index + 1).toVector().clone();

                    lookDir = currentTarget.clone().add(nextTarget).multiply(0.5).subtract(current);
                }
                else
                    lookDir = pathPoints.get(index).toVector().clone().subtract(current);

                Vector horizontalVec = new Vector(lookDir.getX(), 0, lookDir.getZ());
                if(horizontalVec.lengthSquared() < 1e-6)
                    horizontalVec = previousMovement.clone();

                float targetYaw = (float) (Math.atan2(horizontalVec.getZ(), horizontalVec.getX()) * 180 / Math.PI - 90);

                while(targetYaw > 180)
                    targetYaw -= 360;
                while(targetYaw < -180)
                    targetYaw += 360;

                float diff = targetYaw - previousYaw;
                if(diff > 180)
                    diff -= 360;
                if(diff < -180)
                    diff += 360;

                float maxTurn = 15f;
                diff = Math.max(-maxTurn, Math.min(maxTurn, diff));

                float yaw = previousYaw + diff;
                previousYaw = yaw;

                previousMovement = horizontalVec.clone();

                Vector targetVec = pathPoints.get(Math.min(index + 1, pathPoints.size() - 1)).toVector().clone().subtract(current);
                double horizontalLen = Math.sqrt(targetVec.getX() * targetVec.getX() + targetVec.getZ() * targetVec.getZ());
                float pitch = (float) (-Math.atan2(targetVec.getY(), horizontalLen) * 180 / Math.PI) / 1.5f;

                RotateHeadPacket rotateHeadPacket = new RotateHeadPacket(serverPlayer, (byte) (yaw * 256 / 360));
                TeleportEntityPacket teleportEntityPacket = new TeleportEntityPacket(serverPlayer,
                        new TeleportEntityPacket.PositionMoveRotation(current, movement, yaw, pitch), Set.of(), onGround);

                TeleportEntityPacket teleportNameTagPacket = null;
                if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
                {
                    Vector nameTagVector = current.clone()
                            .add(new Vector(0, (serverPlayer.getBoundingBox().getYSize() * getOption(NpcOption.SCALE)), 0));
                    teleportNameTagPacket = new TeleportEntityPacket(serverPlayer.getNameTag(),
                            new TeleportEntityPacket.PositionMoveRotation(nameTagVector, movement, 0, 0), Set.of(), false);
                }

                sendNpcMovePackets(player, teleportEntityPacket, rotateHeadPacket, teleportNameTagPacket);
            }

            @Override
            public synchronized void cancel() throws IllegalStateException
            {
                super.cancel();
                if(onEnd != null)
                    onEnd.accept(Result.CANCELLED);
            }
        }.runTaskTimer(NpcApi.plugin, 1L, 1L);
    }

    /**
     * Sends NPC movement and rotation packets to a specific player or all viewers.
     *
     * @param player                The player to send packets to. If null, packets are sent to all viewers.
     * @param teleportEntityPacket  The packet containing the NPC's teleport/move data. Must not be null.
     * @param rotateHeadPacket      The packet containing the NPC's head rotation data. Must not be null.
     * @param teleportNameTagPacket Optional packet for teleporting the NPC's name tag. Can be null.
     */
    private void sendNpcMovePackets(@Nullable Player player, @NotNull TeleportEntityPacket teleportEntityPacket,
            @NotNull RotateHeadPacket rotateHeadPacket, @Nullable TeleportEntityPacket teleportNameTagPacket)
    {
        if(player != null)
        {
            WrappedServerPlayer serverPlayer1 = WrappedServerPlayer.fromPlayer(player);
            serverPlayer1.sendPacket(teleportEntityPacket);
            serverPlayer1.sendPacket(rotateHeadPacket);

            if(teleportNameTagPacket != null)
                serverPlayer1.sendPacket(teleportNameTagPacket);
        }
        else
        {
            for(UUID uuid : viewers)
            {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if(!offlinePlayer.isOnline())
                    continue;

                WrappedServerPlayer serverPlayer1 = WrappedServerPlayer.fromPlayer(offlinePlayer.getPlayer());
                serverPlayer1.sendPacket(teleportEntityPacket);
                serverPlayer1.sendPacket(rotateHeadPacket);

                if(teleportNameTagPacket != null)
                    serverPlayer1.sendPacket(teleportNameTagPacket);
            }
        }
    }

    void changeUUID(@NotNull UUID newUUID)
    {
        try
        {
            Files.deleteIfExists(npcPath);
            npcPath = NpcApi.plugin.getDataFolder().toPath().resolve("NPC").resolve(newUUID + ".npc");
            save();
        } catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * A serializable representation of an NPC, used for saving and loading NPC data.
     * This record stores all essential properties of an NPC that need to be persisted.
     *
     * @param world      The UUID of the world where the NPC is located.
     * @param x          The x-coordinate of the NPC's location.
     * @param y          The y-coordinate of the NPC's location.
     * @param z          The z-coordinate of the NPC's location.
     * @param yaw        The yaw (horizontal rotation) of the NPC.
     * @param pitch      The pitch (vertical rotation) of the NPC.
     * @param id         The unique identifier (UUID) of the NPC.
     * @param name       The serialized representation of the NPC's display name.
     * @param options    A map of NPC options, where keys are option paths (strings) and values are serializable option values.
     * @param clickEvent The click action associated with the NPC. Can be null.
     * @param createdAt  The timestamp when the NPC was originally created.
     */
    public record SerializedNPC(@NotNull UUID world, double x, double y, double z, float yaw, float pitch, @NotNull UUID id,
                                @NotNull WrappedComponent.SerializedComponent name, @NotNull Map<String, ? extends Serializable> options,
                                @Nullable NpcClickAction clickEvent, @NotNull Instant createdAt) implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Creates a {@link SerializedNPC} instance from an existing {@link NPC} object.
         *
         * @param npc The NPC to serialize. Must not be null.
         * @return A new {@link SerializedNPC} instance representing the given NPC. Will not be null.
         */
        public static @NotNull SerializedNPC serializedNPC(@NotNull NPC npc)
        {
            return new SerializedNPC(npc.getLocation().getWorld().getUID(), npc.getLocation().getX(), npc.getLocation().getY(),
                    npc.getLocation().getZ(), npc.getLocation().getYaw(), npc.getLocation().getPitch(), npc.getUUID(),
                    npc.getName().serialize(), npc.options.keySet().stream().collect(
                    Collectors.toMap(NpcOption::getPath, npcOption -> npcOption.serialize(npc.getOption((NpcOption<?, ?>) npcOption)))),
                    npc.clickEvent, npc.createdAt);
        }

        /**
         * Deserializes this {@link SerializedNPC} object back into a fully functional {@link NPC} instance.
         *
         * @param <T> The type of the NpcOption value.
         * @param <S> The serializable type of the NpcOption value.
         * @return A new {@link NPC} instance reconstructed from the serialized data. Will not be null.
         */
        @SuppressWarnings("unchecked")
        public <T, S extends Serializable> @NotNull NPC deserializedNPC()
        {
            NPC npc = new NPC(new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch), id, name.deserialize()).setClickEvent(
                    clickEvent == null ? clickEvent : clickEvent.initialize());
            options.forEach((string, serializable) -> NpcOption.getOption(string)
                    .ifPresent(npcOption -> npc.setOption((NpcOption<T, S>) npcOption, (T) npcOption.deserialize(Var.unsafeCast(serializable)))));
            npc.createdAt = createdAt == null ? Instant.now() : createdAt;
            return npc;
        }
    }
}
