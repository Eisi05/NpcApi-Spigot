package de.eisi05.npc.api.wrapper.objects;

import com.mojang.authlib.GameProfile;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.utils.exceptions.VersionNotFound;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.enums.Pose;
import de.eisi05.npc.api.wrapper.packets.BundlePacket;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.server.level.ServerPlayer")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_11), path = "net.minecraft.server.level.EntityPlayer")
public class WrappedServerPlayer extends WrappedEntity<Player>
{
    private static final Map<UUID, WrappedServerPlayer> map = new HashMap<>();

    private WrappedNameTag<?> nameDisplay;

    WrappedServerPlayer(Object serverPlayer)
    {
        super(serverPlayer);
    }

    public static @NotNull WrappedServerPlayer fromPlayer(@NotNull Player player)
    {
        return fromEntity(player, WrappedServerPlayer.class);
    }

    public static @NotNull WrappedServerPlayer create(@NotNull Location location, @NotNull UUID uuid, @NotNull GameProfile gameProfile,
                                                      @NotNull WrappedComponent name, @Nullable WrappedNameTag<?> nameDisplay)
    {
        if(map.containsKey(uuid) && nameDisplay == null)
            return map.get(uuid);

        Object mcServer = WrappedMinecraftServer.INSTANCE.getHandle();
        Object serverLevel = Var.getNmsLevel(location.getWorld());

        Class<?> targetClass = PacketWrapper.getWrappedClass(WrappedServerPlayer.class);
        if(targetClass == null)
            throw new VersionNotFound(targetClass);

        WrappedServerPlayer wrappedServerPlayer = switch(Versions.getVersion())
        {
            case V1_20_4, V1_20_6, V1_21, V1_21_2, V1_21_4, V1_21_5, V1_21_6, V1_21_9, V1_21_11, V26_1 ->
            {
                WrappedServerPlayer serverPlayer = createWrappedInstance(WrappedServerPlayer.class, mcServer, serverLevel, gameProfile,
                        WrappedClientInformation.createDefault().getHandle());

                serverPlayer.setConnection(new WrappedConnection(WrappedMinecraftServer.INSTANCE,
                        WrappedConnection.WrappedNetworkManager.create(WrappedConnection.WrappedNetworkManager.PacketFlow.SERVERBOUND),
                        serverPlayer, new WrappedConnection.CommonListenerCookie(gameProfile, 0, true)));

                yield serverPlayer;
            }
            default -> throw new VersionNotFound();
        };

        if(nameDisplay != null)
            wrappedServerPlayer.setNameTag(nameDisplay);

        WrappedTextDisplay textDisplay = WrappedTextDisplay.create(location.getWorld());
        textDisplay.moveTo(location.clone().add(0, 0.2, 0));
        wrappedServerPlayer.setNameTag(textDisplay);

        wrappedServerPlayer.moveTo(location);
        wrappedServerPlayer.setListName(name);
        map.put(uuid, wrappedServerPlayer);
        return wrappedServerPlayer;
    }

    public static @NotNull WrappedServerPlayer create(@NotNull Location location, @NotNull UUID uuid, @NotNull WrappedComponent name,
                                                      @Nullable WrappedNameTag<?> nameDisplay)
    {
        return create(location, uuid, new GameProfile(uuid, "NPC" + uuid.toString().substring(0, 13)), name, nameDisplay);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getAttribute")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_11), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "f")
    public @NotNull WrappedAttributeInstance getAttribute(Object attribute)
    {
        return new WrappedAttributeInstance(invokeWrappedMethod(attribute));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getGameProfile")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "gI")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "gz")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "gr")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "gi")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "gh")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "fX")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "gb")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "fR")
    public @NotNull GameProfile getGameProfile()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "connection")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_11), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21), path = "c")
    public @NotNull WrappedConnection playerConnection()
    {
        return new WrappedConnection(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "connection")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_11), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21), path = "c")
    public void setConnection(@NotNull WrappedConnection newConnection)
    {
        setWrappedFieldValue(newConnection.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V26_1), path = "listName")
    public void setListName(@Nullable WrappedComponent component)
    {
        setWrappedFieldValue(component.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V26_1), path = "listName")
    public void setListName(@Nullable String name)
    {
        setListName(WrappedComponent.create(name));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V26_1), path = "listOrder")
    public void setListOrder(int order)
    {
        setWrappedFieldValue(order);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setPose")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_11), path = "b")
    public void setPose(@NotNull Pose pose)
    {
        invokeWrappedMethod(pose);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "createCommandSourceStack")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "C")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "A")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "z")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "A")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "dg")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "dd")
    public WrappedCommandSourceStack createCommandStack()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "stopUsingItem")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "gf")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_9), path = "fU")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "fM")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "fF")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "fx")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "fB")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "ft")
    public void stopUsingItem()
    {
        invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "sendSystemMessage")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_11), path = "a")
    public void sendRawMessage(@NotNull WrappedComponent component)
    {
        invokeWrappedMethod(component);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "remove")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_11), path = "a")
    public void remove()
    {
        map.remove(getUUID());

        Object nmsLevel = Var.getNmsLevel(getWorld());
        Reflections.invokeMethod(nmsLevel, getPath(), getHandle(), RemovalReason.DISCARDED.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getAdvancements-stopListening")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_9, to = Versions.V1_21_11), path = "U-a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_6), path = "S-a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "R-a")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "S-a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "R-a")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_20_6), path = "Q-a")
    public void stopAdvancementListening()
    {
        String path = getPath();
        String[] paths = path.split("-");

        Reflections.invokeMethod(getHandle(), paths[0]).thanInvoke(paths[1]);
    }

    public @NotNull String getName()
    {
        return (String) Reflections.getField(getGameProfile(), "name").get();
    }

    public @NotNull UUID getUUID()
    {
        return (UUID) Reflections.getField(getGameProfile(), "id").get();
    }

    public boolean isOnGround()
    {
        return ((Entity) getBukkitPlayer()).isOnGround();
    }

    public void sendPacket(@NotNull PacketWrapper packet)
    {
        //if(packet instanceof SetEntityDataPacket dataPacket)
        //    System.out.println(dataPacket.data);

        if(packet instanceof BundlePacket bundlePacket)
            Arrays.stream(bundlePacket.getPackets()).forEach(this::sendPacket);
        else
            playerConnection().sendPacket(packet);
    }

    public WrappedNameTag<?> getNameTag()
    {
        return nameDisplay;
    }

    public void setNameTag(WrappedNameTag<?> nameDisplay)
    {
        setPassengers(nameDisplay);
        this.nameDisplay = nameDisplay;
    }
}
