package de.eisi05.npc.api.objects;

import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.enums.SkinParts;
import de.eisi05.npc.api.utils.ItemSerializer;
import de.eisi05.npc.api.utils.TriFunction;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.enums.ChatFormat;
import de.eisi05.npc.api.wrapper.objects.*;
import de.eisi05.npc.api.wrapper.packets.*;
import org.bukkit.ChatColor;
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
import java.util.stream.Collectors;

/**
 * Represents a configurable option for an NPC.
 * Each option has a path, a default value, serialization/deserialization logic,
 * and a function to generate a network packet for applying the option.
 *
 * @param <T> The type of the option's value in its usable form.
 * @param <S> The type of the option's value in its serialized form.
 */
public class NpcOption<T, S extends Serializable>
{
    /**
     * NPC option to determine if the NPC should use the skin of the viewing player.
     * If true, the NPC's skin will be dynamically set to the skin of the player looking at it.
     */
    public static final NpcOption<Boolean, Boolean> USE_PLAYER_SKIN = new NpcOption<>("use-player-skin", false,
            aBoolean -> aBoolean, aBoolean -> aBoolean,
            (skin, npc, player) ->
            {
                if(!skin)
                    return null;

                var textureProperties = WrappedServerPlayer.fromPlayer(player).getGameProfile().getProperties().get("textures").iterator();
                npc.getServerPlayer().getGameProfile().getProperties().removeAll("textures");

                if(!textureProperties.hasNext())
                    return null;

                var textureProperty = textureProperties.next();
                npc.getServerPlayer().getGameProfile().getProperties().put("textures", textureProperty);
                return null;
            });

    /**
     * NPC option to set a specific skin using a value and signature.
     * This is ignored if {@link #USE_PLAYER_SKIN} is true.
     */
    public static final NpcOption<Skin, Skin> SKIN = new NpcOption<>("skin", null,
            skin -> skin, skin -> skin,
            (skin, npc, player) ->
            {
                if(npc.getOption(USE_PLAYER_SKIN))
                    return null;

                npc.getServerPlayer().getGameProfile().getProperties().removeAll("textures");

                if(skin == null)
                    return null;

                npc.getServerPlayer().getGameProfile().getProperties()
                        .put("textures", new Property("textures", skin.value(), skin.signature()));
                return null;
            });

    /**
     * NPC option to control whether the NPC is shown in the player tab list.
     * If false, the NPC will be removed from the tab list for the viewing player after a short delay.
     */
    public static final NpcOption<Boolean, Boolean> SHOW_TAB_LIST = new NpcOption<>("show-tab-list", true,
            aBoolean -> aBoolean, aBoolean -> aBoolean,
            (show, npc, player) ->
            {
                if(show)
                    return null;

                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        if(Versions.isCurrentVersionSmallerThan(Versions.V1_19_3))
                            WrappedServerPlayer.fromPlayer(player).sendPacket(
                                    new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.REMOVE_PLAYER, npc.getServerPlayer()));
                        else
                            WrappedServerPlayer.fromPlayer(player)
                                    .sendPacket(new PlayerInfoRemovePacket(List.of(npc.getUUID())));
                    }
                }.runTaskLater(NpcApi.plugin, 50);
                return null;
            });

    /**
     * NPC option to set the simulated latency (ping) of the NPC in the tab list.
     */
    public static final NpcOption<Integer, Integer> LATENCY = new NpcOption<>("latency", 0,
            aInteger -> aInteger, aInteger -> aInteger,
            (latency, npc, player) ->
            {
                npc.getServerPlayer().setLatency(latency);

                return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY, npc.getServerPlayer());
            });

    /**
     * NPC option to control the visibility of the NPC's nametag.
     */
    public static final NpcOption<Boolean, Boolean> HIDE_NAMETAG = new NpcOption<>("hide-nametag", false,
            aBoolean -> aBoolean, aBoolean -> aBoolean,
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
     * NPC option to set the pose of the NPC (e.g., standing, sleeping, swimming).
     * For a full list look at {@link Pose}.
     */
    public static final NpcOption<Pose, Pose> POSE = new NpcOption<>("pose", Pose.STANDING,
            pose -> pose, pose -> pose,
            (pose, npc, player) ->
            {
                de.eisi05.npc.api.wrapper.enums.Pose nmsPose = de.eisi05.npc.api.wrapper.enums.Pose.fromBukkit(pose);

                if(nmsPose == null)
                    throw new RuntimeException("Pose (" + pose.name() + ") not found");

                npc.getServerPlayer().setPose(nmsPose);

                WrappedEntityData data = npc.getServerPlayer().getEntityData();
                data.set(WrappedEntityData.EntityDataSerializers.ENTITY_POSE.create(6), nmsPose);

                if(pose == Pose.SPIN_ATTACK)
                    data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(8), (byte) 0x04);
                else
                    data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(8), (byte) 0x01);

                return SetEntityDataPacket.create(npc.getServerPlayer().getId(), data);
            });

    /**
     * NPC option to set the equipment worn by the NPC (armor, items in hand).
     * The map uses {@link EquipmentSlot} as keys and {@link ItemStack} as values.
     * Serialized form uses item base64 strings.
     */
    public static final NpcOption<Map<EquipmentSlot, ItemStack>, HashMap<EquipmentSlot, String>> EQUIPMENT = new NpcOption<>("equipment", Map.of(),
            map -> map.entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,
                            entry -> ItemSerializer.itemStackToBase64(entry.getValue()), (a, b) -> b, HashMap::new)),
            serializedMap -> serializedMap.entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,
                            entry -> ItemSerializer.itemStackFromBase64(entry.getValue()), (a, b) -> b, HashMap::new)),
            (map, npc, player) ->
            {
                if(map.isEmpty())
                    return null;

                List<Pair<?, ?>> list = new ArrayList<>();

                map.forEach((slot, item) -> list.add(
                        new Pair<>(de.eisi05.npc.api.wrapper.enums.EquipmentSlot.values()[slot.ordinal()].getHandle(),
                                Var.toNmsItemStack(item))));

                return new SetEquipmentPacket(npc.getServerPlayer().getId(), list);
            });

    /**
     * NPC option to control which parts of the NPC's skin are visible (e.g., hat, jacket).
     * For a full list look at {@link SkinParts}.
     */
    public static final NpcOption<SkinParts[], SkinParts[]> SKIN_PARTS = new NpcOption<>("skin-parts", SkinParts.values(),
            skinParts -> skinParts, skinParts -> skinParts,
            (skinParts, npc, player) ->
            {
                WrappedEntityData data = npc.getServerPlayer().getEntityData();
                data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(17),
                        (byte) Arrays.stream(skinParts).mapToInt(SkinParts::getValue).sum());
                return SetEntityDataPacket.create(npc.getServerPlayer().getId(), data);
            });

    /**
     * NPC option to make the NPC look at the player if they are within a certain distance.
     * The value is the maximum distance in blocks. A value of 0 or less disables this.
     * The actual looking logic is handled by {@link Tasks#lookAtTask()}.
     */
    public static final NpcOption<Double, Double> LOOK_AT_PLAYER = new NpcOption<>("look-at-player", 0.0,
            distance -> distance, distance -> distance,
            (distance, npc, player) -> null);

    /**
     * NPC option to make the NPC glow with a specific color.
     * If null, the glowing effect is removed.
     */
    public static final NpcOption<ChatColor, ChatColor> GLOWING = new NpcOption<>("glowing", null,
            color -> color, color -> color,
            (color, npc, player) ->
            {
                if(color == null)
                {
                    WrappedEntityData entityData = npc.getServerPlayer().getEntityData();
                    entityData.set(WrappedEntityData.EntityDataSerializers.BYTE.create(0), (byte) 0);
                    return SetEntityDataPacket.create(npc.getServerPlayer().getId(), entityData);
                }

                String teamName = npc.getServerPlayer().getName();
                boolean modified = WrappedPlayerTeam.exists(player, teamName);
                WrappedPlayerTeam wrappedPlayerTeam = WrappedPlayerTeam.create(player, teamName);

                wrappedPlayerTeam.setColor(ChatFormat.fromChatColor(color));

                var teamPacket = SetPlayerTeamPacket.createAddOrModifyPacket(wrappedPlayerTeam, !modified);

                WrappedEntityData entityData = npc.getServerPlayer().getEntityData();
                entityData.set(WrappedEntityData.EntityDataSerializers.BYTE.create(0), (byte) 0x40);

                return new BundlePacket(teamPacket, SetEntityDataPacket.create(npc.getServerPlayer().getId(), entityData));
            });

    /**
     * NPC option to set the scale (size) of the NPC.
     * A value of 1.0 is normal size. Requires Minecraft 1.20.6 or newer.
     */
    public static final NpcOption<Double, Double> SCALE = new NpcOption<>("scale", 1.0,
            scale -> scale, scale -> scale,
            (scale, npc, player) ->
            {
                WrappedAttributeInstance instance = npc.getServerPlayer().getAttribute(WrappedAttributeInstance.Attributes.SCALE_HOLDER);
                instance.setBaseValue(scale);

                return new UpdateAttributesPacket(npc.getServerPlayer().getId(), instance);
            }).since(Versions.V1_20_6);

    /**
     * NPC option to control if the NPC is enabled (visible and interactable).
     * If false, a "DISABLED" marker may be shown.
     * This is an internal option, typically not directly set by users but controlled by {@link NPC#setEnabled(boolean)}.
     */
    static final NpcOption<Boolean, Boolean> ENABLED = new NpcOption<>("enabled", false,
            aBoolean -> aBoolean, aBoolean -> aBoolean,
            (enabled, npc, player) ->
            {
                if(enabled)
                    return null;

                WrappedArmorStand armorStand = WrappedArmorStand.create(npc.getLocation().getWorld());
                armorStand.moveTo(
                        npc.getLocation().clone().add(0, (npc.getServerPlayer().getBoundingBox().getYSize() * npc.getOption(SCALE)) + 0.3, 0));

                PacketWrapper addPacket = armorStand.getAddEntityPacket();

                WrappedEntityData data = armorStand.getEntityData();

                data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(0), (byte) 0x20);
                data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2),
                        Optional.of(WrappedComponent.create("DISABLED").setFormats(ChatFormat.RED).getHandle()));
                data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(3), true);
                data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(4), true);
                data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(15), (byte) 0x10);

                npc.toDeleteEntities.add(armorStand.getId());

                return new BundlePacket(addPacket, SetEntityDataPacket.create(armorStand.getId(), data));
            });

    private final String path;
    private final T defaultValue;
    private final Function<T, S> serializer;
    private final Function<S, T> deserializer;
    private final TriFunction<T, NPC, Player, PacketWrapper> packet;
    private Versions since = Versions.V1_17;

    /**
     * Private constructor to create a new NpcOption.
     *
     * @param path         The configuration path string. Must not be null.
     * @param defaultValue The default value for the option. Can be null.
     * @param serializer   The serialization function. Must not be null.
     * @param deserializer The deserialization function. Must not be null.
     * @param packet       The packet generation function. Must not be null.
     */
    private NpcOption(@NotNull String path, @Nullable T defaultValue, @NotNull Function<T, S> serializer, @NotNull Function<S, T> deserializer,
            @NotNull TriFunction<T, NPC, Player, PacketWrapper> packet)
    {
        this.path = path;
        this.defaultValue = defaultValue;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.packet = packet;
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
            } catch(IllegalAccessException e)
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
     * Sets the minimum Minecraft version required for this option.
     * Used for options that are only available in newer versions of the game.
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
     * Checks if this NPC option is compatible with the current server version.
     * An option is compatible if the current server version is greater than or equal to
     * the version specified by {@link #since()}.
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
        return defaultValue;
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
     * @throws RuntimeException if a {@link ClassCastException} occurs during serialization,
     *                          indicating an incorrect type was passed.
     */
    @SuppressWarnings("unchecked")
    public @Nullable S serialize(@Nullable Object var1)
    {
        try
        {
            return serializer.apply((T) var1);
        } catch(ClassCastException e)
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
     * Generates the network packet(s) needed to apply this option's value to an NPC for a specific player.
     * The method checks for version compatibility before generating the packet.
     *
     * @param object The value of the option to apply. Can be null.
     * @param npc    The {@link NPC} to apply the option to. Must not be null.
     * @param player The {@link Player} who will receive the update. Must not be null.
     * @return An {@link Optional} containing the {@link PacketWrapper} if one is generated and the option is compatible,
     * otherwise an empty Optional.
     */
    @SuppressWarnings("unchecked")
    public @NotNull Optional<PacketWrapper> getPacket(@Nullable Object object, @NotNull NPC npc, @NotNull Player player)
    {
        if(packet == null || !isCompatible())
            return Optional.empty();

        return Optional.ofNullable(packet.apply((T) object, npc, player));
    }
}
