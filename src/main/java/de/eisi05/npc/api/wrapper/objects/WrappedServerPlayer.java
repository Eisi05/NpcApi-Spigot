package de.eisi05.npc.api.wrapper.objects;

import com.mojang.authlib.GameProfile;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.utils.exceptions.VersionNotFound;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.enums.Pose;
import de.eisi05.npc.api.wrapper.packets.BundlePacket;
import de.eisi05.npc.api.wrapper.packets.ChatPacket;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import de.eisi05.npc.api.wrapper.packets.SetEntityDataPacket;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.server.level.EntityPlayer")
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

    public static @NotNull WrappedServerPlayer create(@NotNull Location location, @NotNull UUID uuid, @NotNull WrappedComponent name)
    {
        if(map.containsKey(uuid))
            return map.get(uuid);

        Object mcServer = WrappedMinecraftServer.INSTANCE.getHandle();
        Object serverLevel = Var.getNmsLevel(location.getWorld());

        GameProfile gameProfile = new GameProfile(uuid, "NPC" + uuid.toString().substring(0, 13));

        Mapping[] annotations = WrappedServerPlayer.class.getAnnotationsByType(Mapping.class);

        Class<?> targetClass = null;
        for(Mapping mapping : annotations)
        {
            if(!Versions.containsCurrentVersion(mapping))
                continue;

            targetClass = getTargetClass(mapping);
            break;
        }

        if(targetClass == null)
            throw new VersionNotFound(targetClass);

        WrappedServerPlayer wrappedServerPlayer = switch(Versions.getVersion())
        {
            case V1_17, V1_18, V1_18_2, V1_19_3, V1_19_4, V1_20 ->
                    createWrappedInstance(WrappedServerPlayer.class, mcServer, serverLevel, gameProfile);
            case V1_19, V1_19_1 -> createWrappedInstance(WrappedServerPlayer.class, mcServer, serverLevel, gameProfile, null);
            case V1_20_2, V1_20_4, V1_20_6, V1_21, V1_21_2, V1_21_4, V1_21_5, V1_21_6 ->
            {
                WrappedServerPlayer serverPlayer = createWrappedInstance(WrappedServerPlayer.class, mcServer, serverLevel, gameProfile,
                        WrappedClientInformation.createDefault().getHandle());

                serverPlayer.setConnection(new WrappedConnection(WrappedMinecraftServer.INSTANCE,
                        WrappedConnection.WrappedNetworkManager.create(WrappedConnection.WrappedNetworkManager.PacketFlow.SERVERBOUND),
                        serverPlayer, new WrappedConnection.CommonListenerCookie(gameProfile, 0, true)));

                yield serverPlayer;
            }
            case NONE -> throw new VersionNotFound();
        };

        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
        {
            WrappedArmorStand armorStand = WrappedArmorStand.create(location.getWorld());
            armorStand.moveTo(location.clone().add(0, 0.2, 0));
            wrappedServerPlayer.setNameTag(armorStand);
        }
        else
        {
            WrappedTextDisplay textDisplay = WrappedTextDisplay.create(location.getWorld());
            textDisplay.moveTo(location.clone().add(0, 0.2, 0));
            wrappedServerPlayer.setNameTag(textDisplay);
        }

        wrappedServerPlayer.setListName(name);
        map.put(uuid, wrappedServerPlayer);
        return wrappedServerPlayer;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_6), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "f")
    public @NotNull WrappedAttributeInstance getAttribute(Object attribute)
    {
        return new WrappedAttributeInstance(invokeWrappedMethod(attribute));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_6), path = "gr")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "gi")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "gh")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21), path = "fX")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_6), path = "gb")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "fR")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "fQ")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "fM")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_4), path = "fI")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "fD")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_1), path = "fy")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19), path = "fz")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_18_2), path = "fq")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_18), path = "fp")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getProfile")
    public @NotNull GameProfile getGameProfile()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_6), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20, to = Versions.V1_21), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_4), path = "b")
    public @NotNull WrappedConnection playerConnection()
    {
        return new WrappedConnection(getWrappedFieldValue());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_6), path = "g")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_5), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20, to = Versions.V1_21), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_4), path = "b")
    public void setConnection(@NotNull WrappedConnection newConnection)
    {
        setWrappedFieldValue(newConnection.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "listName")
    public void setListName(@Nullable WrappedComponent component)
    {
        setWrappedFieldValue(component.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "listName")
    public void setListName(@Nullable String name)
    {
        setListName(WrappedComponent.create(name));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_6), path = "listOrder")
    public void setListOrder(int order)
    {
        setWrappedFieldValue(order);
    }

    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "f")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_19_4), path = "e")
    public void setLatency(int i)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_20_2))
            setWrappedFieldValue(i);
        else
            setConnection(new WrappedConnection(WrappedMinecraftServer.INSTANCE, playerConnection().networkManager(), this,
                    new WrappedConnection.CommonListenerCookie(getGameProfile(), i, true)));
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_6), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setPose")
    public void setPose(@NotNull Pose pose)
    {
        invokeWrappedMethod(pose);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_6, to = Versions.V1_21_6), path = "A")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_5), path = "z")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_2, to = Versions.V1_21_4), path = "A")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21), path = "dg")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_4), path = "dd")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20_2), path = "dc")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_20), path = "da")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_4), path = "cZ")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_3), path = "cY")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19_1), path = "cT")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_19), path = "cU")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_18_2), path = "cQ")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getCommandListener")
    public WrappedCommandSourceStack createCommandStack()
    {
        return new WrappedCommandSourceStack(invokeWrappedMethod());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_6), path = "a")
    public void sendRawMessage(@NotNull WrappedComponent component)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19))
            sendPacket(new ChatPacket(component, ChatPacket.ChatType.CHAT));
        else
            invokeWrappedMethod(component);
    }

    public @NotNull String getName()
    {
        return getGameProfile().getName();
    }

    public @NotNull UUID getUUID()
    {
        return getGameProfile().getId();
    }

    public boolean isOnGround()
    {
        return ((Entity) getBukkitPlayer()).isOnGround();
    }

    public void sendPacket(@NotNull PacketWrapper packet)
    {
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
