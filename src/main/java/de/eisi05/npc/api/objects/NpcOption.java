package de.eisi05.npc.api.objects;

import com.google.common.collect.Multimaps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.enums.NpcVisibility;
import de.eisi05.npc.api.enums.SkinParts;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.manager.NpcVisibilityManager;
import de.eisi05.npc.api.scheduler.Tasks;
import de.eisi05.npc.api.utils.*;
import de.eisi05.npc.api.wrapper.enums.ChatFormat;
import de.eisi05.npc.api.wrapper.objects.*;
import de.eisi05.npc.api.wrapper.packets.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a configurable option for an NPC. Each option has a path, a default value, serialization/deserialization logic, and a function to generate a
 * network packet for applying the option.
 *
 * @param <T> The type of the option's value in its usable form.
 * @param <S> The type of the option's value in its serialized form.
 */
public class NpcOption<T, S extends Serializable>
{
    /**
     * NPC option to determine if the NPC should use the skin of the viewing player. If true, the NPC's skin will be dynamically set to the skin of the player
     * looking at it.
     */
    public static final NpcOption<Boolean, Boolean> USE_PLAYER_SKIN = new NpcOption<>("use-player-skin", () -> false,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean,
            (skin, npc, player) ->
            {
                if(!skin || !npc.serverPlayer.equals(npc.entity))
                    return null;

                if(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_9))
                {
                    var textureProperties = ((PropertyMap) Reflections.getField(WrappedServerPlayer.fromPlayer(player).getGameProfile(), "properties")
                            .get()).get("textures").iterator();

                    var npcTextureProperties = ((PropertyMap) Reflections.getField(npc.getServerPlayer().getGameProfile(), "properties")
                            .get()).get("textures").iterator();

                    Property property = textureProperties.hasNext() ? textureProperties.next() : null;
                    Property npcProperty = npcTextureProperties.hasNext() ? npcTextureProperties.next() : null;

                    if((property == null && npcProperty == null) || (property != null && npcProperty != null &&
                            Reflections.getField(property, "value")
                                    .get()
                                    .equals(Reflections.getField(npcProperty, "value").get())))
                        return null;

                    GameProfile profile = Reflections.getInstance(GameProfile.class, npc.getUUID(), "NPC" + npc.getUUID().toString().substring(0, 13),
                            Reflections.getInstance(PropertyMap.class, Multimaps.forMap(property == null ? Map.of() : Map.of("textures", property)))
                                    .orElseThrow()).orElseThrow();

                    int id = npc.serverPlayer.getId();
                    NpcManager.removeNPC(npc);
                    npc.serverPlayer = WrappedServerPlayer.create(npc.getLocation(), npc.getUUID(), profile, npc.getName(player),
                            npc.getServerPlayer().getNameTag());
                    npc.serverPlayer.setId(id);
                    NpcManager.addNPC(npc);
                    return null;
                }

                var textureProperties = WrappedServerPlayer.fromPlayer(player).getGameProfile().getProperties().get("textures").iterator();
                npc.getServerPlayer().getGameProfile().getProperties().removeAll("textures");

                if(!textureProperties.hasNext())
                    return null;

                var textureProperty = textureProperties.next();
                npc.getServerPlayer().getGameProfile().getProperties().put("textures", textureProperty);
                return null;
            }).loadBefore(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_9));

    /**
     * NPC option to set a specific skin using a value and signature. This is ignored if {@link #USE_PLAYER_SKIN} is true.
     */
    public static final NpcOption<NpcSkin, SkinData> SKIN = new NpcOption<NpcSkin, SkinData>("skin", () -> null,
            skin -> skin, skin -> skin, skin -> skin instanceof Skin skin1 ? NpcSkin.of(skin1) : (NpcSkin) skin,
            (skinData, npc, player) ->
            {
                if(npc.getOption(USE_PLAYER_SKIN, player) || skinData == null || !npc.serverPlayer.equals(npc.entity))
                    return null;

                Skin skin = skinData.getSkin(player, npc);
                if(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_9))
                {
                    var npcTextureProperties = ((PropertyMap) Reflections.getField(npc.getServerPlayer().getGameProfile(), "properties")
                            .get()).get("textures").iterator();

                    Property npcProperty = npcTextureProperties.hasNext() ? npcTextureProperties.next() : null;
                    if(skin != null && npcProperty != null && skin.value().equals(Reflections.getField(npcProperty, "value").get()))
                        return null;

                    PropertyMap propertyMap = Reflections.getInstance(PropertyMap.class,
                            Multimaps.forMap(skin == null ? Map.of() : Map.of("textures", new Property("textures", skin.value(),
                                    skin.signature())))).orElseThrow();

                    GameProfile profile = Reflections.getInstance(GameProfile.class, npc.getUUID(), "NPC" + npc.getUUID().toString().substring(0, 13),
                            propertyMap).orElseThrow();

                    int id = npc.serverPlayer.getId();
                    NpcManager.removeNPC(npc);
                    npc.serverPlayer = WrappedServerPlayer.create(npc.getLocation(), npc.getUUID(), profile, npc.getName(player),
                            npc.getServerPlayer().getNameTag());
                    npc.serverPlayer.setId(id);
                    NpcManager.addNPC(npc);
                    return null;
                }

                npc.getServerPlayer().getGameProfile().getProperties().removeAll("textures");

                if(skin == null)
                    return null;

                npc.getServerPlayer().getGameProfile().getProperties().put("textures", new Property("textures", skin.value(), skin.signature()));
                return null;
            }).loadBefore(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_9));

    /**
     * NPC option to control whether the NPC is shown in the player tab list. If false, the NPC will be removed from the tab list for the viewing player after a
     * short delay.
     */
    public static final NpcOption<Boolean, Boolean> SHOW_TAB_LIST = new NpcOption<>("show-tab-list", () -> true,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean,
            (show, npc, player) ->
            {
                if(!show || !npc.name.isStatic())
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            if(player != null && npc.getUUID() != null)
                                WrappedServerPlayer.fromPlayer(player).sendPacket(new PlayerInfoRemovePacket(List.of(npc.getUUID())));
                        }
                    }.runTaskLater(NpcApi.plugin, 50);
                }
                return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, npc.getServerPlayer());
            }).loadBefore(true);

    /**
     * NPC option to set the simulated latency (ping) of the NPC in the tab list.
     */
    public static final NpcOption<Integer, Integer> LATENCY = new NpcOption<>("latency", () -> 0,
            aInteger -> aInteger, aInteger -> aInteger, aInteger -> aInteger,
            (latency, npc, player) ->
                    new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY, npc.getServerPlayer()));

    /**
     * NPC option to control the visibility of the NPC's nametag.
     */
    public static final NpcOption<Boolean, Boolean> HIDE_NAMETAG = new NpcOption<>("hide-nametag", () -> false,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean,
            (hide, npc, player) ->
            {
                WrappedEntity<?> nameTag = npc.getServerPlayer().getNameTag();

                WrappedEntityData data = npc.getServerPlayer().getNameTag().getEntityData();
                if(nameTag instanceof WrappedArmorStand)
                    data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(3), !hide);
                else if(nameTag instanceof WrappedTextDisplay)
                {
                    if(!hide)
                        return null;

                    return new RemoveEntityPacket(npc.getServerPlayer().getNameTag().getId());
                }

                return SetEntityDataPacket.create(npc.getServerPlayer().getNameTag().getId(), data);
            });

    /**
     * NPC option to set the equipment worn by the NPC (armor, items in hand). The map uses {@link EquipmentSlot} as keys and {@link ItemStack} as values.
     * Serialized form uses item base64 strings.
     */
    public static final NpcOption<Map<EquipmentSlot, ItemStack>, HashMap<EquipmentSlot, String>> EQUIPMENT = new NpcOption<>("equipment", HashMap::new, HashMap::new,
            map ->
            {
                HashMap<EquipmentSlot, String> serializedMap = new HashMap<>();
                map.forEach((slot, item) ->
                {
                    if(item == null || item.getType().isAir())
                        return;

                    String serialized = ItemSerializer.itemStackToBase64(item);
                    if(serialized == null)
                        return;

                    serializedMap.put(slot, serialized);
                });
                return serializedMap;
            },
            serializedMap ->
            {
                HashMap<EquipmentSlot, ItemStack> map = new HashMap<>();
                serializedMap.forEach((slot, string) ->
                {
                    if(string == null || string.isEmpty())
                        return;

                    ItemStack item = ItemSerializer.itemStackFromBase64(string);
                    if(item == null)
                        return;

                    map.put(slot, item);
                });
                return map;
            },
            (map, npc, player) ->
            {
                if(map.isEmpty())
                    return null;

                List<Pair<?, ?>> list = new ArrayList<>();

                map.forEach((slot, item) -> list.add(
                        new Pair<>(de.eisi05.npc.api.wrapper.enums.EquipmentSlot.values()[slot.ordinal()].getHandle(), Var.toNmsItemStack(item))));

                return new SetEquipmentPacket(npc.entity.getId(), list);
            });

    /**
     * NPC option to control which parts of the NPC's skin are visible (e.g., hat, jacket). For a full list look at {@link SkinParts}.
     */
    public static final NpcOption<SkinParts[], SkinParts[]> SKIN_PARTS = new NpcOption<>("skin-parts", SkinParts::values,
            skinParts -> skinParts, skinParts -> skinParts, skinParts -> skinParts,
            (skinParts, npc, player) ->
            {
                WrappedEntityData data = npc.getServerPlayer().getEntityData();
                data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(Versions.isCurrentVersionSmallerThan(Versions.V1_21_9) ? 17 : 16),
                        (byte) Arrays.stream(skinParts).mapToInt(SkinParts::getValue).sum());
                return SetEntityDataPacket.create(npc.getServerPlayer().getId(), data);
            });

    /**
     * NPC option to make the NPC look at the player if they are within a certain distance. The value is the maximum distance in blocks. A value of 0 or less
     * disables this. The actual looking logic is handled by {@link Tasks#lookAtTask()}.
     */
    public static final NpcOption<Double, Double> LOOK_AT_PLAYER = new NpcOption<>("look-at-player", () -> 0.0,
            distance -> distance, distance -> distance, distance -> distance,
            (distance, npc, player) -> null);

    /**
     * NPC option to control visibility with three states: fully visible, transparent, or invisible.
     * <p>
     * <b>Important Notes:</b>
     * <ul>
     *   <li>When TRANSPARENT is enabled, it affects other team-based options (COLLISION, GLOWING) by forcing them
     *   to use the same transparent team naming convention</li>
     * </ul>
     * <p>
     */
    public static final NpcOption<NpcVisibility, NpcVisibility> VISIBILITY = new NpcOption<>("visibility", () -> NpcVisibility.FULLY_VISIBLE,
            visibility -> visibility, visibility -> visibility, visibility -> visibility, (visibility, npc, player) ->
    {
        String teamName = "trans-" + player.getEntityId();
        WrappedPlayerTeam playerTeam = WrappedPlayerTeam.getPlayersTeam(player);

        boolean modified = WrappedPlayerTeam.exists(player, teamName) || playerTeam.getHandle() != null;
        WrappedPlayerTeam wrappedPlayerTeam = playerTeam.getHandle() != null ? playerTeam : WrappedPlayerTeam.create(player, teamName);
        wrappedPlayerTeam.setCanSeeFriendlyInvisible(true);
        wrappedPlayerTeam.setNameTagVisibility(WrappedPlayerTeam.Visibility.HIDE_FOR_OWN_TEAM);

        var teamPacket = SetPlayerTeamPacket.createAddOrModifyPacket(wrappedPlayerTeam, !modified);

        WrappedEntityData entityData = npc.entity.getEntityData();
        WrappedEntityData.EntityDataAccessor<Byte> accessor = WrappedEntityData.EntityDataSerializers.BYTE.create(0);
        Byte value = entityData.get(accessor);
        byte flags = value == null ? 0 : value;

        byte modifier = 0x20;

        List<PacketWrapper> packets = new ArrayList<>(List.of(teamPacket));

        var removeNpc = SetPlayerTeamPacket.createPlayerPacket(wrappedPlayerTeam, npc.getServerPlayer().getName(), SetPlayerTeamPacket.Action.REMOVE);

        if(wrappedPlayerTeam.getPlayers().remove(npc.getServerPlayer().getName()))
            packets.add(removeNpc);

        if(visibility == NpcVisibility.FULLY_VISIBLE)
        {
            entityData.set(accessor, (byte) (flags & ~modifier));
            packets.add(SetEntityDataPacket.create(npc.entity.getId(), entityData));
            return new BundlePacket(packets.toArray(new PacketWrapper[0]));
        }

        entityData.set(accessor, (byte) (flags | modifier));

        if(visibility == NpcVisibility.INVISIBLE)
        {
            packets.add(SetEntityDataPacket.create(npc.entity.getId(), entityData));
            return new BundlePacket(packets.toArray(new PacketWrapper[0]));
        }

        wrappedPlayerTeam.getPlayers().add(npc.getServerPlayer().getName());
        var addNpc = SetPlayerTeamPacket.createPlayerPacket(wrappedPlayerTeam, npc.getServerPlayer().getName(), SetPlayerTeamPacket.Action.ADD);
        var addPlayer = SetPlayerTeamPacket.createPlayerPacket(wrappedPlayerTeam, player.getName(), SetPlayerTeamPacket.Action.ADD);

        return new BundlePacket(teamPacket, SetEntityDataPacket.create(npc.entity.getId(), entityData), addNpc, addPlayer);
    });

    /**
     * NPC option to make the NPC glow with a specific color. If null, the glowing effect is removed.
     * <p>
     * <b>Important Notes:</b>
     * <ul>
     *   <li>If TRANSPARENT is enabled, this option will change glowing for all npc with TRANSPARENT enabled</li>
     * </ul>
     * </p>
     */
    public static final NpcOption<ChatColor, ChatColor> GLOWING = new NpcOption<>("glowing", () -> null,
            color -> color, color -> color, color -> color,
            (color, npc, player) ->
            {
                WrappedEntityData entityData = npc.entity.getEntityData();
                WrappedEntityData.EntityDataAccessor<Byte> accessor = WrappedEntityData.EntityDataSerializers.BYTE.create(0);
                Byte value = entityData.get(accessor);
                byte flags = value == null ? 0 : value;

                byte modifier = 0x40;

                if(color == null)
                {
                    entityData.set(accessor, (byte) (flags & ~modifier));
                    return SetEntityDataPacket.create(npc.entity.getId(), entityData);
                }

                var teamPair = getTeam(player, npc);
                WrappedPlayerTeam team = teamPair.getFirst();

                team.setColor(ChatFormat.fromChatColor(color));

                var teamPacket = SetPlayerTeamPacket.createAddOrModifyPacket(team, !teamPair.getSecond());

                entityData.set(accessor, (byte) (flags | modifier));

                return new BundlePacket(teamPacket, SetEntityDataPacket.create(npc.entity.getId(), entityData));
            });

    /**
     * NPC option to control collision behavior between players and NPCs. When enabled, players can push and collide with NPCs. When disabled, players pass
     * through NPCs without collision.
     * <p>
     * <b>Important Notes:</b>
     * <ul>
     *   <li>If TRANSPARENT is enabled, this option will change collisions for all npc with TRANSPARENT enabled</li>
     * </ul>
     * <p>
     */
    public static final NpcOption<Boolean, Boolean> COLLISION = new NpcOption<>("collision", () -> true,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean, (collision, npc, player) ->
    {
        var teamPair = getTeam(player, npc);
        WrappedPlayerTeam team = teamPair.getFirst();
        team.setCollisionRule(collision ? WrappedPlayerTeam.CollisionRule.ALWAYS : WrappedPlayerTeam.CollisionRule.NEVER);
        return SetPlayerTeamPacket.createAddOrModifyPacket(team, !teamPair.getSecond());
    });

    /**
     * NPC option to set the pose of the NPC (e.g., standing, sleeping, swimming). For a full list look at {@link Pose}.
     */
    public static final NpcOption<Pose, Pose> POSE = new NpcOption<>("pose", () -> Pose.STANDING,
            pose -> pose, pose -> pose, pose -> pose,
            (pose, npc, player) ->
            {
                de.eisi05.npc.api.wrapper.enums.Pose nmsPose = de.eisi05.npc.api.wrapper.enums.Pose.fromBukkit(pose);

                if(nmsPose == null)
                    throw new RuntimeException("Pose (" + pose.name() + ") not found");

                npc.getServerPlayer().setPose(nmsPose);

                WrappedEntityData data = npc.entity.getEntityData();
                data.set(WrappedEntityData.EntityDataSerializers.ENTITY_POSE.create(6), nmsPose);

                WrappedEntityData.EntityDataAccessor<Byte> accessor = WrappedEntityData.EntityDataSerializers.BYTE.create(0);
                Byte value = data.get(accessor);
                byte flags = value == null ? 0 : value;

                PacketWrapper packetWrapper = null;
                if(pose == Pose.FALL_FLYING)
                {
                    data.set(accessor, (byte) (flags | 0x80));
                    packetWrapper = new MoveEntityPacket.Rot(npc.entity.getId(), (byte) (npc.getLocation().getYaw() * 256 / 360),
                            (byte) 0, npc.getServerPlayer().isOnGround());
                }
                else if(pose == Pose.SWIMMING)
                    data.set(accessor, (byte) (flags | 0x10));
                else
                    data.set(accessor, (byte) (flags & ~(0x80 | 0x10)));

                if(!npc.entity.getBukkitPlayer().getType().name().equals("ITEM_DISPLAY") &&
                        !npc.entity.getBukkitPlayer().getType().name().equals("BLOCK_DISPLAY"))
                {
                    Byte handValue = data.get(WrappedEntityData.EntityDataSerializers.BYTE.create(8));
                    byte handFlag = handValue == null ? 0 : handValue;

                    if(pose == Pose.SPIN_ATTACK)
                    {
                        data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(8), (byte) (handFlag |  0x04));
                        packetWrapper = new MoveEntityPacket.Rot(npc.entity.getId(), (byte) (npc.getLocation().getYaw() * 256 / 360),
                                (byte) -90, npc.getServerPlayer().isOnGround());
                    }
                    else
                        data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(8), (byte) (handFlag & ~0x04));
                }

                if(pose == Pose.SLEEPING)
                {
                    new BukkitRunnable()
                    {
                        int counter = 255;
                        final Location startLocation = npc.getLocation().clone();
                        final float startYaw = startLocation.getYaw();

                        @Override
                        public void run()
                        {
                            startLocation.setYaw((startYaw + counter) % 360);
                            npc.updateLocationForPlayer(startLocation, player);
                            if(counter == 360)
                                cancel();

                            counter += 35;
                        }
                    }.runTaskTimer(NpcApi.plugin, 10, 5);
                }
                else
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            npc.updateLocationForPlayer(npc.getLocation(), player);
                        }
                    }.runTaskLater(NpcApi.plugin, 1);
                }

                Integer oldId = npc.toDeleteEntities.remove("sit");
                if(nmsPose == de.eisi05.npc.api.wrapper.enums.Pose.SITTING)
                {
                    WrappedTextDisplay textDisplay = WrappedTextDisplay.create(npc.getLocation().getWorld());
                    textDisplay.moveTo(npc.getLocation());
                    npc.toDeleteEntities.put("sit", textDisplay.getId());

                    PacketWrapper addEntityPacket = textDisplay.getAddEntityPacket();

                    WrappedEntityData wrappedEntityData = textDisplay.getEntityData();
                    wrappedEntityData.set(accessor, (byte) (flags | 0x20));
                    SetEntityDataPacket entityDataPacket = SetEntityDataPacket.create(textDisplay.getId(), wrappedEntityData);

                    textDisplay.setPassengers(npc.entity);

                    SetPassengerPacket passengerPacket = new SetPassengerPacket(textDisplay);
                    RotateHeadPacket rotateHeadPacket = new RotateHeadPacket(npc.entity, (byte) (npc.getLocation().getYaw() * 256 / 360));

                    if(oldId == null)
                        return new BundlePacket(addEntityPacket, entityDataPacket, passengerPacket, rotateHeadPacket);

                    return new BundlePacket(new RemoveEntityPacket(oldId), addEntityPacket, entityDataPacket, passengerPacket, rotateHeadPacket);
                }
                else
                {
                    if(oldId == null)
                        return packetWrapper == null ? new BundlePacket(SetEntityDataPacket.create(npc.entity.getId(), data)) :
                                new BundlePacket(packetWrapper, SetEntityDataPacket.create(npc.entity.getId(), data));

                    return packetWrapper == null ?
                            new BundlePacket(new RemoveEntityPacket(oldId), SetEntityDataPacket.create(npc.entity.getId(), data)) :
                            new BundlePacket(packetWrapper, new RemoveEntityPacket(oldId), SetEntityDataPacket.create(npc.entity.getId(), data));
                }
            });

    /**
     * NPC option to set the scale (size) of the NPC. A value of 1.0 is normal size. Requires Minecraft 1.20.6 or newer.
     */
    public static final NpcOption<Double, Double> SCALE = new NpcOption<>("scale", () -> 1.0,
            scale -> scale, scale -> scale, scale -> scale,
            (scale, npc, player) ->
            {
                if(npc.entity.getBukkitPlayer().getType().name().equals("ITEM_DISPLAY") ||
                        npc.entity.getBukkitPlayer().getType().name().equals("BLOCK_DISPLAY"))
                    return null;

                WrappedAttributeInstance instance = npc.getServerPlayer().getAttribute(WrappedAttributeInstance.Attributes.SCALE_HOLDER);
                instance.setBaseValue(scale);

                return new UpdateAttributesPacket(npc.entity.getId(), instance);
            }).since(Versions.V1_20_6);

    /**
     * NPC option to control the position of the NPC in the TAB list.
     * <p>
     * Only works on versions older than 1.21.2. On 1.21.2 and newer, this option has no effect.
     * </p>
     */
    public static final NpcOption<Integer, Integer> LIST_ORDER = new NpcOption<>("list-order", () -> 0,
            aInt -> aInt, aInt -> aInt, aInt -> aInt,
            (order, npc, player) ->
            {
                if(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_2))
                    return null;

                npc.getServerPlayer().setListOrder(order);

                return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER, npc.getServerPlayer());
            }).since(Versions.V1_21_2);

    /**
     * NPC option to change the entity type of the NPC. This allows transforming the NPC into any Minecraft entity type. The default is a PLAYER entity. When
     * changed, the NPC will be recreated as the new entity type.
     *
     * <p>Note: Some entity types may not work as expected, and certain NPC features may be limited
     * depending on the entity type. The NPC will maintain its position, name, and other properties when changing entity types.</p>
     *
     * <p>Example usage to change to a zombie:
     * <pre>{@code
     * npc.setOption(NpcOption.ENTITY, new WrappedEntitySnapshot(EntityType.ZOMBIE));
     * }</pre>
     */
    public static final NpcOption<WrappedEntitySnapshot, WrappedEntitySnapshot> ENTITY = new NpcOption<>("entity",
            () -> new WrappedEntitySnapshot(EntityType.PLAYER), wrappedEntitySnapshot -> wrappedEntitySnapshot,
            wrappedEntitySnapshot -> wrappedEntitySnapshot, wrappedEntitySnapshot -> wrappedEntitySnapshot,
            (wrappedEntitySnapshot, npc, player) ->
            {
                WrappedEntity<?> entity;
                if(wrappedEntitySnapshot.getType() == EntityType.PLAYER || wrappedEntitySnapshot.getType().name().equals("MANNEQUIN") ||
                        wrappedEntitySnapshot.getType() == EntityType.UNKNOWN)
                    entity = npc.serverPlayer;
                else if(wrappedEntitySnapshot.getType() != npc.entity.getBukkitPlayer().getType() ||
                        !npc.entity.data.equals(wrappedEntitySnapshot.getData().toString()))
                {
                    entity = wrappedEntitySnapshot.create(player.getWorld());
                    entity.moveTo(npc.getLocation());
                    npc.toDeleteEntities.put("entity", entity.getId());
                }
                else
                    entity = npc.entity;

                npc.entity = entity;
                NpcManager.addID(npc.entity.getId(), npc);

                List<PacketWrapper> packets = new ArrayList<>();

                packets.add(new RemoveEntityPacket(npc.getServerPlayer().getId()));
                packets.add(entity.getAddEntityPacket());

                var teamPair = getTeam(player, npc);
                WrappedPlayerTeam team = teamPair.getFirst();
                team.setNameTagVisibility(WrappedPlayerTeam.Visibility.NEVER);

                if(!team.getName().startsWith("trans"))
                    team.getPlayers().add(npc.getServerPlayer().getName());
                else
                    (WrappedPlayerTeam.create(player, npc.getServerPlayer().getName())).getPlayers().remove(npc.getServerPlayer().getName());

                packets.add(SetPlayerTeamPacket.createAddOrModifyPacket(team, !teamPair.getSecond()));
                packets.add(SetPlayerTeamPacket.createPlayerPacket(team, npc.getServerPlayer().getName(), SetPlayerTeamPacket.Action.ADD));
                packets.add(SetPlayerTeamPacket.createPlayerPacket(team, npc.entity.getBukkitPlayer().getUniqueId().toString(),
                        SetPlayerTeamPacket.Action.ADD));

                WrappedEntityData data = entity.getEntityData();

                data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(0),
                        (byte) (Var.nbtToEntityFlags(wrappedEntitySnapshot.getData()) | Var.extractFlagsFromBukkit(entity.getBukkitPlayer())));
                data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2), Optional.of(WrappedComponent.create("NPC").getHandle()));
                data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(3), false);
                packets.add(SetEntityDataPacket.create(entity.getId(), data));

                if(!npc.getOption(NpcOption.HIDE_NAMETAG, player))
                {
                    if(Versions.isCurrentVersionSmallerThan(Versions.V1_21))
                        npc.serverPlayer.getNameTag()
                                .moveTo(npc.getLocation().clone().add(0,
                                        (entity.getBoundingBox().getYSize() * npc.getOption(NpcOption.SCALE, player)), 0));

                    packets.add(npc.serverPlayer.getNameTag().getAddEntityPacket());

                    packets.add(SetEntityDataPacket.create(npc.serverPlayer.getNameTag().getId(), npc.serverPlayer.getNameTag().applyData(
                            npc.isEnabled() ? npc.name.getName(player) :
                                    WrappedComponent.parseFromLegacy(NpcApi.DISABLED_MESSAGE_PROVIDER.apply(player))
                                    .append(WrappedComponent.create("\n").append(npc.name.getName(player))))));

                    entity.setPassengers(npc.serverPlayer.getNameTag());
                    packets.add(new SetPassengerPacket(entity));
                }

                return new BundlePacket(packets.toArray(new PacketWrapper[0]));
            }).loadBefore(true);

    /**
     * NPC option to control if the NPC is enabled (visible and interactable). If false, a "DISABLED" marker may be shown. This is an internal option, typically
     * not directly set by users but controlled by {@link NPC#setEnabled(boolean)}.
     */
    static final NpcOption<Boolean, Boolean> ENABLED = new NpcOption<>("enabled", () -> false,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean,
            (enabled, npc, player) ->
            {
                return null;
            });

    /**
     * NPC option to control if the NPC is enabled (visible and interactable). If false, a "DISABLED" marker may be shown. This is an internal option, typically
     * not directly set by users but controlled by {@link NPC#setEnabled(boolean)}.
     */
    static final NpcOption<Boolean, Boolean> EDITABLE = new NpcOption<>("editable", () -> false,
            aBoolean -> aBoolean, aBoolean -> aBoolean, aBoolean -> aBoolean,
            (enabled, npc, player) -> null);

    /**
     * NPC option to store the goal selector for the NPC. This allows saving and restoring the NPC's AI behavior. The serialized form stores the running state,
     * tick interval, and goal configurations. Note: Goals themselves are not fully serialized - only their class names and any serializable configuration. On
     * deserialization, goals must be re-instantiated by the plugin.
     */
    static final NpcOption<ArrayList<Goal>, ArrayList<Goal>> GOALS = new NpcOption<>("goals", ArrayList::new,
            goals -> new ArrayList<>(goals.stream().map(Goal::copy).toList()), goals -> goals,
            goals -> goals,
            (data, npc, player) -> null);

    /**
     * NPC option to store custom data for the NPC. This is an internal option, typically not directly set by users but controlled by
     * {@link NPC#addCustomData(Serializable, Serializable)}.
     */
    static final NpcOption<HashMap<Serializable, Serializable>, HashMap<Serializable, Serializable>> CUSTOM_DATA = new NpcOption<>("custom-data",
            HashMap::new, HashMap::new,
            aHashMap -> aHashMap, aHashMap -> aHashMap,
            (customData, npc, player) -> null);

    /**
     * NPC option to manage visibility settings for the NPC. This controls whether the NPC should be shown to all players (including new ones) or only to specific players.
     */
    static final NpcOption<NpcVisibilityManager, NpcVisibilityManager> VISIBILITY_MANAGER = new NpcOption<>("visibility-manager", NpcVisibilityManager::new,
            npcVisibilityManager ->
            {
                NpcVisibilityManager newManager = new NpcVisibilityManager();
                newManager.setShowToAllPlayers(npcVisibilityManager.shouldShowToAllPlayers());
                npcVisibilityManager.getSpecificPlayers().forEach(newManager::addSpecificPlayer);
                return newManager;
            },visibilityManager -> visibilityManager, visibilityManager ->
            {
                if(visibilityManager.getSpecificPlayers().isEmpty() && !visibilityManager.shouldShowToAllPlayers())
                    visibilityManager.setShowToAllPlayers(true);

                return visibilityManager;
            },
            (visibilityManager, npc, player) -> null);

    private final String path;
    private final Supplier<T> defaultValue;
    private final Function<T, S> serializer;
    private final Function<S, T> deserializer;
    private final Function<T, T> copyFunction;
    private final TriFunction<T, NPC, Player, PacketWrapper> packet;
    private Versions since = Versions.V1_20_6;
    private boolean loadBefore = false;

    /**
     * Private constructor to create a new NpcOption.
     *
     * @param path         The configuration path string. Must not be null.
     * @param defaultValue The default value for the option. Can be null.
     * @param copyFunction The copy function. Must not be null.
     * @param serializer   The serialization function. Must not be null.
     * @param deserializer The deserialization function. Must not be null.
     * @param packet       The packet generation function. Must not be null.
     */
    private NpcOption(@NotNull String path, @Nullable Supplier<T> defaultValue, @NotNull Function<T, T> copyFunction,
                      @NotNull Function<T, S> serializer, @NotNull Function<S, T> deserializer, @NotNull TriFunction<T, NPC, Player, PacketWrapper> packet)
    {
        this.path = path;
        this.defaultValue = defaultValue;
        this.copyFunction = copyFunction;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.packet = packet;
    }

    private static @NotNull Pair<WrappedPlayerTeam, Boolean> getTeam(@NotNull Player player, @NotNull NPC npc)
    {
        boolean isTransparent = npc.getOption(VISIBILITY, player) == NpcVisibility.TRANSPARENT;
        WrappedPlayerTeam playerTeam = WrappedPlayerTeam.getPlayersTeam(player);
        String teamName = isTransparent ? "trans-" + player.getEntityId() : npc.getServerPlayer().getName();
        boolean modified = WrappedPlayerTeam.exists(player, teamName) || (playerTeam.getHandle() != null && isTransparent);
        WrappedPlayerTeam wrappedPlayerTeam = playerTeam.getHandle() != null && isTransparent ? playerTeam : WrappedPlayerTeam.create(player, teamName);
        return new Pair<>(wrappedPlayerTeam, modified);
    }

    /**
     * Retrieves all declared {@link NpcOption} constants within this class using reflection.
     *
     * @return An array of {@link NpcOption} instances. Will not be null.
     */
    public static @NotNull NpcOption<?, ?>[] values()
    {
        List<Field> fields = Arrays.stream(NpcOption.class.getDeclaredFields()).filter(field -> field.getType().equals(NpcOption.class)).toList();

        NpcOption<?, ?>[] values = new NpcOption[fields.size()];

        for(int i = 0; i < fields.size(); i++)
        {
            try
            {
                values[i] = (NpcOption<?, ?>) fields.get(i).get(null);
            }
            catch(IllegalAccessException e)
            {
            }
        }

        return values;
    }

    /**
     * Retrieves an {@link NpcOption} instance by its configuration path.
     *
     * @param path The configuration path string to search for. Must not be null.
     * @return An {@link Optional} containing the found {@link NpcOption}, or an empty Optional if no option matches the path.
     */
    public static @NotNull Optional<NpcOption<?, ?>> getOption(@NotNull String path)
    {
        return Arrays.stream(values()).filter(npcOption -> npcOption.getPath().equals(path)).findFirst();
    }

    /**
     * Sets the minimum Minecraft version required for this option. Used for options that are only available in newer versions of the game.
     *
     * @param since The minimum {@link Versions} required. Must not be null.
     * @return This {@link NpcOption} instance for method chaining.
     */
    public @NotNull NpcOption<T, S> since(@NotNull Versions since)
    {
        this.since = since;
        return this;
    }

    /**
     * Sets whether this option should be loaded before other options.
     *
     * @param loadBefore Whether this option should be loaded before other options.
     * @return This {@link NpcOption} instance for method chaining.
     */
    public @NotNull NpcOption<T, S> loadBefore(boolean loadBefore)
    {
        this.loadBefore = loadBefore;
        return this;
    }

    /**
     * Returns whether this option should be loaded before other options.
     *
     * @return {@code true} if this option should be loaded before other options, {@code false} otherwise.
     */
    public boolean loadBefore()
    {
        return loadBefore;
    }

    /**
     * Checks if this NPC option is compatible with the current server version. An option is compatible if the current server version is greater than or equal
     * to the version specified by {@link #since()}.
     *
     * @return {@code true} if the option is compatible, {@code false} otherwise.
     */
    public boolean isCompatible()
    {
        return !Versions.isCurrentVersionSmallerThan(since);
    }

    /**
     * Gets the minimum Minecraft version required for this option.
     *
     * @return The {@link Versions} instance.
     */
    public Versions since()
    {
        return since;
    }

    /**
     * Gets the default value for this option.
     *
     * @return The default value, which can be null if defined as such.
     */
    public @Nullable T getDefaultValue()
    {
        return defaultValue.get();
    }

    /**
     * Creates a copy of the current value
     *
     * @param value The current value which should be copied
     * @return A copy of the current value
     */
    public @NotNull T copy(@NotNull T value)
    {
        return copyFunction.apply(value);
    }

    /**
     * Gets the configuration path string for this option.
     *
     * @return The path string. Will not be null.
     */
    public @NotNull String getPath()
    {
        return path;
    }

    /**
     * Serializes the given value (of type T) into its serializable form (type S).
     *
     * @param var1 The value to serialize. Can be null.
     * @return The serialized value. Can be null if the input or serializer result is null.
     * @throws RuntimeException if a {@link ClassCastException} occurs during serialization, indicating an incorrect type was passed.
     */
    @SuppressWarnings("unchecked")
    public @Nullable S serialize(@Nullable Object var1)
    {
        try
        {
            return serializer.apply((T) var1);
        }
        catch(ClassCastException e)
        {
            throw new RuntimeException(path + " -> " + var1);
        }
    }

    /**
     * Deserializes the given value (of type S) back into its usable form (type T).
     *
     * @param var1 The serialized value to deserialize. Can be null.
     * @return The deserialized value. Can be null if the input or deserializer result is null.
     */
    public @Nullable T deserialize(@Nullable S var1)
    {
        return deserializer.apply(var1);
    }

    /**
     * Generates the network packet(s) needed to apply this option's value to an NPC for a specific player. The method checks for version compatibility before
     * generating the packet.
     *
     * @param object The value of the option to apply. Can be null.
     * @param npc    The {@link NPC} to apply the option to. Must not be null.
     * @param player The {@link Player} who will receive the update. Must not be null.
     * @return An {@link Optional} containing the {@link PacketWrapper} if one is generated and the option is compatible, otherwise an empty Optional.
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<PacketWrapper> getPacket(@Nullable Object object, @NotNull NPC npc, @NotNull Player player)
    {
        if(packet == null || !isCompatible())
            return Optional.empty();

        return Optional.ofNullable(packet.apply((T) object, npc, player));
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof NpcOption<?, ?> npcOption))
            return false;

        return npcOption.getPath().equals(getPath());
    }

    @Override
    public int hashCode()
    {
        return getPath().hashCode();
    }
}