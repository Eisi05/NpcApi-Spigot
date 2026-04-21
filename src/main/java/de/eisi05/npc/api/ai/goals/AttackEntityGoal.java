package de.eisi05.npc.api.ai.goals;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.ai.Goal;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.utils.LocationUtils;
import de.eisi05.npc.api.utils.SerializablePredicate;
import de.eisi05.npc.api.wrapper.enums.Pose;
import de.eisi05.npc.api.wrapper.objects.WrappedEntityData;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import de.eisi05.npc.api.wrapper.packets.AnimatePacket;
import de.eisi05.npc.api.wrapper.packets.PacketWrapper;
import de.eisi05.npc.api.wrapper.packets.RemoveEntityPacket;
import de.eisi05.npc.api.wrapper.packets.SetEntityDataPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.*;

/**
 * A goal that makes the NPC attack nearby entities that match a predicate. The attack behavior varies based on the item in the NPC's main hand: -
 * Bow/Trident/Crossbow: Long range attacks - Sword/Axe: Short range attacks - Other: Short range attacks Only activates when a valid target is in range and
 * line of sight. Once a target is locked, the NPC continues attacking until the target becomes unreachable or invalid.
 */
public class AttackEntityGoal extends Goal
{
    @Serial
    private static final long serialVersionUID = 1L;

    private static final double BOW_ATTACK_RANGE = 15.0;
    private static final double MELEE_ATTACK_RANGE = 3.0;
    private static final double LINE_OF_SIGHT_RANGE = 25.0;
    private static final int ATTACK_COOLDOWN_TICKS = 15;
    private static final int BOW_DRAW_DELAY_TICKS = 25;
    private static final double ATTACK_SPEED_MULTIPLIER = 0.5;

    private final SerializablePredicate<LivingEntity> targetFilter;
    private final double customAttackRange;

    private transient LivingEntity target;
    private transient int attackCooldown;
    private transient boolean isAttacking;
    private transient WalkToLocationGoal movementGoal;
    private transient boolean isUsing;
    private transient List<Player> cachedViewers;
    private transient int lineOfSightCheckCooldown;
    private transient int pathRecalculationCooldown;

    /**
     * Creates an AttackEntityGoal with a filter for valid targets.
     *
     * @param targetFilter A predicate to filter valid targets
     */
    public AttackEntityGoal(@NotNull SerializablePredicate<LivingEntity> targetFilter)
    {
        this(targetFilter, -1);
    }

    /**
     * Creates an AttackEntityGoal with a filter for valid targets and custom attack range.
     *
     * @param targetFilter      A predicate to filter valid targets
     * @param customAttackRange Custom attack range (-1 for default based on weapon)
     */
    public AttackEntityGoal(@NotNull SerializablePredicate<LivingEntity> targetFilter, double customAttackRange)
    {
        super(Priority.ALWAYS);
        this.targetFilter = targetFilter;
        this.customAttackRange = customAttackRange;
    }

    @Override
    public boolean canUse(@NotNull NPC npc)
    {
        if(target != null && target.isValid() && canContinue(npc))
            return true;

        LivingEntity potentialTarget = findTarget(npc);
        if(potentialTarget == null)
            return false;

        this.target = potentialTarget;
        return true;
    }

    @Override
    public void start(@NotNull NPC npc)
    {
        this.attackCooldown = 0;
        this.isAttacking = true;
        movementGoal = null;
        isUsing = false;
        lineOfSightCheckCooldown = 0;
        pathRecalculationCooldown = 0;
        setUsingItemState(npc, false);
        updateCachedViewers(npc);
    }

    @Override
    public void tick(@NotNull NPC npc)
    {
        if(target == null || !target.isValid())
        {
            isAttacking = false;
            stopMovement(npc);
            return;
        }

        Location npcLoc = npc.getLocation();

        if(cachedViewers == null || cachedViewers.size() != npc.getViewers().size())
            updateCachedViewers(npc);

        for(Player viewer : cachedViewers)
            npc.lookAtEntity(target, viewer, true);

        if(lineOfSightCheckCooldown > 0)
            lineOfSightCheckCooldown--;

        double distance = npcLoc.distance(target.getLocation());
        double attackRange = getAttackRange(npc);

        if(distance > attackRange)
        {
            boolean checkLineOfSight = lineOfSightCheckCooldown <= 0;
            if(checkLineOfSight)
                lineOfSightCheckCooldown = 5;

            if(distance > LINE_OF_SIGHT_RANGE || (checkLineOfSight && !hasLineOfSight(npc, target)))
            {
                stop(npc);
                return;
            }

            if(movementGoal == null)
                startMovement(npc, target.getLocation());
            else
            {
                Location currentTarget = movementGoal.getTargetLocation();
                boolean shouldRecalculate = currentTarget.distance(target.getLocation()) > 5.0;

                if(!shouldRecalculate && pathRecalculationCooldown <= 0)
                {
                    shouldRecalculate = true;
                    pathRecalculationCooldown = WalkToLocationGoal.RECALCULATION_COOLDOWN;
                }

                if(shouldRecalculate)
                {
                    startMovement(npc, target.getLocation());
                    pathRecalculationCooldown = WalkToLocationGoal.RECALCULATION_COOLDOWN;
                }
                else
                {
                    movementGoal.tick(npc);
                    pathRecalculationCooldown--;
                }

                distance = npc.getLocation().distance(target.getLocation());
                if(distance <= attackRange)
                    stopMovement(npc);
                else
                    return;
            }
        }
        else
            stopMovement(npc);

        if(attackCooldown > 0)
        {
            attackCooldown--;
            return;
        }

        if(distance <= attackRange)
        {
            performAttack(npc);
            attackCooldown = getAttackCooldown(npc);
        }
    }

    @Override
    public void stop(@NotNull NPC npc)
    {
        stopMovement(npc);
        target = null;
        isAttacking = false;
        attackCooldown = 0;
        isUsing = false;
        setUsingItemState(npc, false);
    }

    @Override
    public boolean canContinue(@NotNull NPC npc)
    {
        if(!isAttacking || target == null || !target.isValid())
            return false;

        Location npcLoc = npc.getLocation();
        Location targetLoc = target.getLocation();

        if(!npcLoc.getWorld().equals(targetLoc.getWorld()))
            return false;

        boolean checkLineOfSight = lineOfSightCheckCooldown <= 0;
        if(checkLineOfSight && !hasLineOfSight(npc, target))
            return false;

        double distance = npcLoc.distance(targetLoc);
        double attackRange = getAttackRange(npc);

        return distance <= Math.max(attackRange, LINE_OF_SIGHT_RANGE);
    }

    /**
     * Finds a valid target entity near the NPC.
     */
    private LivingEntity findTarget(@NotNull NPC npc)
    {
        Location npcLoc = npc.getLocation();
        double searchRange = Math.max(getAttackRange(npc), LINE_OF_SIGHT_RANGE);

        for(Entity entity : npcLoc.getWorld().getNearbyEntities(npcLoc, searchRange, searchRange, searchRange))
        {
            if(!(entity instanceof LivingEntity livingEntity))
                continue;

            if(!targetFilter.test(livingEntity))
                continue;

            if(!hasLineOfSight(npc, livingEntity))
                continue;

            return livingEntity;
        }

        return null;
    }

    /**
     * Checks if the NPC has line of sight to the target entity.
     */
    private boolean hasLineOfSight(@NotNull NPC npc, @NotNull LivingEntity target)
    {
        Location npcLoc = npc.getLocation().clone().add(0, 1.6, 0);
        Location targetLoc = target.getLocation().clone().add(0, target.getEyeHeight(), 0);

        Vector direction = targetLoc.toVector().subtract(npcLoc.toVector()).normalize();
        double distance = npcLoc.distance(targetLoc);
        int steps = (int) (distance * 2);

        for(int i = 0; i < steps; i++)
        {
            Location checkLoc = npcLoc.clone().add(direction.clone().multiply(i * 0.5));
            if(checkLoc.getBlock().getType().isSolid() && !checkLoc.getBlock().isPassable())
                return false;
        }

        return true;
    }

    /**
     * Gets the attack range based on the NPC's held item.
     */
    private double getAttackRange(@NotNull NPC npc)
    {
        if(customAttackRange > 0)
            return customAttackRange;

        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;

        if(mainHand == null)
            return MELEE_ATTACK_RANGE;

        Material type = mainHand.getType();
        if(isRangedWeapon(type))
            return BOW_ATTACK_RANGE;
        else
            return MELEE_ATTACK_RANGE;
    }

    /**
     * Gets the attack cooldown based on the NPC's held item.
     */
    private int getAttackCooldown(@NotNull NPC npc)
    {
        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;

        if(mainHand == null)
            return ATTACK_COOLDOWN_TICKS;

        Material type = mainHand.getType();
        if(isRangedWeapon(type))
            return ATTACK_COOLDOWN_TICKS;
        else
            return (int) (ATTACK_COOLDOWN_TICKS * ATTACK_SPEED_MULTIPLIER);
    }

    /**
     * Performs an attack on the target.
     */
    private void performAttack(@NotNull NPC npc)
    {
        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;

        if(mainHand != null && isRangedWeapon(mainHand.getType()))
            performRangedAttack(npc);
        else
            performMeleeAttack(npc);
    }

    /**
     * Performs a ranged attack.
     */
    private void performRangedAttack(@NotNull NPC npc)
    {
        if(target == null || !target.isValid() || isUsing)
            return;

        double eyeHeight = (npc.entity.getBukkitPlayer() instanceof LivingEntity le ? le.getEyeHeight() : npc.entity.getBukkitPlayer().getHeight()) -
                (Pose.fromBukkit(npc.getOption(NpcOption.POSE)) == Pose.SITTING ? 0.625 : 0);

        Location npcLoc = npc.getLocation().clone().add(0, eyeHeight * npc.getOption(NpcOption.SCALE), 0);

        final float speed = 2.5f;

        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;

        if(mainHand != null && (mainHand.getType() == Material.BOW || mainHand.getType() == Material.CROSSBOW))
        {
            setUsingItemState(npc, true);
            isUsing = true;

            Bukkit.getScheduler().runTaskLater(NpcApi.plugin, () ->
            {
                setUsingItemState(npc, false);

                isUsing = false;

                if(target == null)
                    return;

                Location targetLoc = target.getLocation().clone().add(0, target.getEyeHeight(), 0);
                Vector direction = targetLoc.toVector().subtract(npcLoc.toVector()).normalize();

                //TODO: Maybe Packets/NMS and also make the arrows invisible

                /*
                Arrow arrow = new Arrow(world, startLocation.getX(), startLocation.getY(), startLocation.getZ()); // net.minecraft.world.entity.projectile.Arrow
                arrow.setDeltaMovement(directionX, directionY, directionZ);
                arrow.setBaseDamage(getAttackDamage(npc));

                // set shooter
                arrow.setOwner(((CraftPlayer) player).getHandle());

                world.addFreshEntity(arrow);


                 */

                AbstractArrow arrow;
                if(mainHand == null || mainHand.getType() == Material.TRIDENT)
                    arrow = npcLoc.getWorld().spawnArrow(npcLoc, direction, speed, 0, Trident.class);
                else
                    arrow = npcLoc.getWorld().spawnArrow(npcLoc, direction, speed, 0, Arrow.class);
                arrow.setShooter(npc.getServerPlayer().getBukkitPlayer());
                arrow.setDamage(getAttackDamage(npc));
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                arrow.setShotFromCrossbow(mainHand.getType() == Material.CROSSBOW);

                PacketWrapper removePacket = new RemoveEntityPacket(arrow.getEntityId());
                for(Player player : npcLoc.getWorld().getPlayers())
                {
                    if(!npc.getViewers().contains(player.getUniqueId()))
                        WrappedServerPlayer.fromPlayer(player).sendPacket(removePacket);
                }
            }, BOW_DRAW_DELAY_TICKS);
        }
    }

    private void setUsingItemState(@NotNull NPC npc, boolean using)
    {
        WrappedEntityData data = npc.getServerPlayer().getEntityData();
        WrappedEntityData.EntityDataAccessor<Byte> accessor = WrappedEntityData.EntityDataSerializers.BYTE.create(8);

        Byte handValue = data.get(accessor);
        byte handFlag = handValue == null ? 0 : handValue;

        handFlag = using ? (byte) (handFlag | 0x01) : (byte) (handFlag & ~0x01);
        data.set(accessor, handFlag);

        if(cachedViewers == null || cachedViewers.size() != npc.getViewers().size())
            updateCachedViewers(npc);

        PacketWrapper packetWrapper = SetEntityDataPacket.create(npc.getServerPlayer().getId(), data);
        for(Player viewer : cachedViewers)
            WrappedServerPlayer.fromPlayer(viewer).sendPacket(packetWrapper);
    }

    /**
     * Performs a melee attack.
     */
    private void performMeleeAttack(@NotNull NPC npc)
    {
        if(target == null || !target.isValid())
            return;

        if(cachedViewers == null || cachedViewers.size() != npc.getViewers().size())
            updateCachedViewers(npc);

        for(Player viewer : cachedViewers)
            npc.playAnimation(viewer, AnimatePacket.Animation.SWING_MAIN_HAND);

        target.damage(getAttackDamage(npc));

        Location npcLoc = npc.getLocation();
        Location targetLoc = target.getLocation();

        double knockbackStrength = getAttackKnockback(npc);
        double knockbackResistance = getKnockbackResistance(target);
        double effectiveKnockback = knockbackStrength * (1 - knockbackResistance);

        Vector knockback = targetLoc.toVector().subtract(npcLoc.toVector()).normalize().multiply(effectiveKnockback);
        knockback.setY(effectiveKnockback * 0.4);
        target.setVelocity(knockback);
    }

    /**
     * Checks if a material is a ranged weapon.
     */
    private boolean isRangedWeapon(@NotNull Material material)
    {
        return material == Material.BOW || material == Material.CROSSBOW || material == Material.TRIDENT;
    }

    /**
     * Gets the attack damage based on the NPC's held item.
     */
    private double getAttackDamage(@NotNull NPC npc)
    {
        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;
        return getAttackDamage(mainHand);
    }

    private double getAttackDamage(ItemStack item)
    {
        if(item == null || !item.hasItemMeta())
            return 0.5;

        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            return 0.5;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
        if(modifiers == null)
            return 0.5;

        double damage = 0;
        for(AttributeModifier modifier : modifiers)
            damage += modifier.getAmount();

        return damage == 0 ? 0.5 : damage;
    }

    private double getAttackKnockback(@NotNull NPC npc)
    {
        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        ItemStack mainHand = equipment != null ? equipment.get(EquipmentSlot.HAND) : null;
        return getAttackKnockback(mainHand);
    }

    private double getAttackKnockback(ItemStack item)
    {
        if(item == null || !item.hasItemMeta())
            return 0.5;

        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            return 0.5;

        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_KNOCKBACK);
        if(modifiers == null)
            return 0.5;

        double knockback = 0;
        for(AttributeModifier modifier : modifiers)
            knockback += modifier.getAmount();

        return knockback == 0 ? 0.5 : knockback;
    }

    private double getKnockbackResistance(@NotNull LivingEntity target)
    {
        if(target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) == null)
            return 0;

        return target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue();
    }

    /**
     * Starts movement towards the target location.
     */
    private void startMovement(@NotNull NPC npc, @NotNull Location targetLoc)
    {
        stopMovement(npc);

        OptionalInt safeY = LocationUtils.findSafeY(targetLoc);
        if(safeY.isPresent())
            targetLoc = new Location(targetLoc.getWorld(), targetLoc.getX(), safeY.getAsInt(), targetLoc.getZ());
        movementGoal = new WalkToLocationGoal(targetLoc, 0.4);
        movementGoal.start(npc);
    }

    /**
     * Updates the cached viewers list.
     */
    private void updateCachedViewers(@NotNull NPC npc)
    {
        cachedViewers = npc.getViewers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Stops any ongoing movement.
     */
    private void stopMovement(@NotNull NPC npc)
    {
        if(movementGoal != null)
        {
            movementGoal.stop(npc);
            movementGoal = null;
        }
    }
}
