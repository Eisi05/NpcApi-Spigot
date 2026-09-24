package de.eisi05.npc.api.listeners;

import com.google.common.collect.Multimap;
import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.enums.ClickActionType;
import de.eisi05.npc.api.events.NpcDamageEvent;
import de.eisi05.npc.api.events.NpcDeathEvent;
import de.eisi05.npc.api.events.NpcInteractEvent;
import de.eisi05.npc.api.manager.NpcCombatManager;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.pathfinding.AbstractPathfinder;
import de.eisi05.npc.api.pathfinding.BoundingBoxPathfinder;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Var;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import de.eisi05.npc.api.wrapper.objects.WrappedMinecraftServer;
import de.eisi05.npc.api.wrapper.packets.AnimatePacket;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class NpcDamageListener implements Listener
{
    private static final float FALLBACK_DAMAGE = 2;

    private static final int INVULNERABLE_DURATION_TICKS = 10;
    private static final double KNOCKBACK_POWER = 0.4;
    private static final double GRAVITY = 0.08;
    private static final double HORIZONTAL_DRAG = 0.91;
    private static final double SETTLE_THRESHOLD = 0.015;

    private static final Map<NPC, CombatState> states = new WeakHashMap<>();
    private static final Map<UUID, Integer> lastAttackTicks = new HashMap<>();

    private static final Map<Projectile, Location> activeProjectiles = new ConcurrentHashMap<>();
    private static BukkitTask projectileTask;

    public static BukkitTask startProjectileTracker()
    {
        if(projectileTask != null)
            return projectileTask;

        return projectileTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if(activeProjectiles.isEmpty())
                    return;

                Iterator<Map.Entry<Projectile, Location>> iterator = activeProjectiles.entrySet().iterator();
                while(iterator.hasNext())
                {
                    Map.Entry<Projectile, Location> entry = iterator.next();
                    Projectile projectile = entry.getKey();
                    Location lastLoc = entry.getValue();

                    if(!projectile.isValid() || projectile.isDead())
                    {
                        iterator.remove();
                        continue;
                    }

                    Location currentLoc = projectile.getLocation();
                    if(!currentLoc.getWorld().equals(lastLoc.getWorld()))
                    {
                        entry.setValue(currentLoc);
                        continue;
                    }

                    Vector travelVector = currentLoc.toVector().subtract(lastLoc.toVector());
                    double distance = travelVector.length();
                    if(distance > 1.0E-4)
                    {
                        Vector direction = travelVector.clone().normalize();
                        NPC hitNpc = rayTraceNpcs(lastLoc, direction, distance);

                        if(hitNpc != null)
                        {
                            iterator.remove();
                            handleProjectileHit(hitNpc, projectile);
                            projectile.remove();
                            continue;
                        }
                    }

                    if(projectile.isOnGround())
                    {
                        iterator.remove();
                        continue;
                    }

                    entry.setValue(currentLoc);
                }
            }
        }.runTaskTimer(NpcApi.plugin, 1L, 1L);
    }

    private static void applyProjectileKnockback(NPC npc, Vector velocity, CombatState state, double knockbackMultiplier)
    {
        if(knockbackMultiplier <= 0)
            return;

        Vector direction = velocity.clone().setY(0);
        if(direction.lengthSquared() < 1.0E-4)
            return;

        direction.normalize();

        double power = KNOCKBACK_POWER * knockbackMultiplier;

        state.vx = state.vx / 2.0 + direction.getX() * power;
        state.vz = state.vz / 2.0 + direction.getZ() * power;
        state.vy = state.grounded ? Math.min(0.4, state.vy / 2.0 + power) : state.vy;

        if(state.task == null)
            startPhysicsTask(npc, state);
    }

    private static void startPhysicsTask(NPC npc, CombatState state)
    {
        WrappedEntity.BoundingBox box = npc.entity.getBoundingBox();
        double scale = npc.getOption(NpcOption.SCALE);
        double width = box.getXSize() * scale;
        double height = box.getYSize() * scale;

        boolean wasRunningGoals = npc.isGoalSystemRunning();
        npc.cancelWalking();
        if(wasRunningGoals)
            npc.stopGoals();

        state.task = new BukkitRunnable()
        {
            int tick = 0;

            @Override
            public void run()
            {
                tick++;
                state.vy -= GRAVITY;

                Location current = npc.getLocation();
                World world = current.getWorld();

                double cx = current.getX(), cy = current.getY(), cz = current.getZ();
                double targetX = cx + state.vx, targetZ = cz + state.vz;

                double resolvedX = cx, resolvedZ = cz;
                if(isBoxClear(world, targetX, cy, targetZ, width, height))
                {
                    resolvedX = targetX;
                    resolvedZ = targetZ;
                }
                else if(isBoxClear(world, targetX, cy, cz, width, height))
                {
                    resolvedX = targetX;
                    state.vz = 0;
                }
                else if(isBoxClear(world, cx, cy, targetZ, width, height))
                {
                    resolvedZ = targetZ;
                    state.vx = 0;
                }
                else
                {
                    state.vx = 0;
                    state.vz = 0;
                }

                double resolvedY;
                state.grounded = false;

                if(state.vy > 0)
                {
                    if(isBoxClear(world, resolvedX, cy + state.vy, resolvedZ, width, height))
                        resolvedY = cy + state.vy;
                    else
                    {
                        resolvedY = cy;
                        state.vy = 0;
                    }
                }
                else
                {
                    double fallTargetY = cy + state.vy;
                    BoundingBoxPathfinder.FootSupport support =
                            BoundingBoxPathfinder.resolveGroundSupport(world, resolvedX, cy, resolvedZ, width, 0.1, 10.0);

                    if(support.valid() && fallTargetY <= support.feetY())
                    {
                        resolvedY = support.feetY();
                        state.vy = 0;
                        state.grounded = true;
                    }
                    else
                        resolvedY = fallTargetY;
                }

                npc.changeRealLocation(new Location(world, resolvedX, resolvedY, resolvedZ, current.getYaw(), current.getPitch()));

                double drag = HORIZONTAL_DRAG;
                if(state.grounded)
                {
                    Block blockBelow = world.getBlockAt((int) Math.floor(resolvedX), (int) Math.floor(resolvedY - 0.1), (int) Math.floor(resolvedZ));
                    drag *= blockBelow.getType().isAir() ? 0.6F : blockBelow.getType().getSlipperiness();
                }

                state.vx *= drag;
                state.vz *= drag;

                double speed = Math.sqrt(state.vx * state.vx + state.vy * state.vy + state.vz * state.vz);

                boolean settled = state.grounded && speed < SETTLE_THRESHOLD;
                boolean outOfBounds = cy < world.getMinHeight();

                if(settled || outOfBounds)
                {
                    cancel();
                    state.task = null;
                    state.vx = state.vy = state.vz = 0;
                    if(wasRunningGoals)
                        npc.startGoals();
                }
            }
        }.runTaskTimer(NpcApi.plugin, 1L, 1L);
    }

    private static boolean isBoxClear(World world, double x, double y, double z, double width, double height)
    {
        double radius = width / 2.0;
        double minX = x - radius, maxX = x + radius;
        double maxY = y + height;
        double minZ = z - radius, maxZ = z + radius;

        int minBX = (int) Math.floor(minX), maxBX = (int) Math.floor(maxX);
        int minBY = (int) Math.floor(y), maxBY = (int) Math.floor(maxY);
        int minBZ = (int) Math.floor(minZ), maxBZ = (int) Math.floor(maxZ);

        for(int bx = minBX; bx <= maxBX; bx++)
        {
            for(int by = minBY; by <= maxBY; by++)
            {
                for(int bz = minBZ; bz <= maxBZ; bz++)
                {
                    Block block = world.getBlockAt(bx, by, bz);
                    Collection<BoundingBox> blockBoxes = AbstractPathfinder.getBlockBoxes(block);
                    if(blockBoxes.isEmpty())
                        continue;

                    for(BoundingBox blockBox : blockBoxes)
                    {
                        double bMinX = blockBox.getMinX() + bx, bMaxX = blockBox.getMaxX() + bx;
                        double bMinY = blockBox.getMinY() + by, bMaxY = blockBox.getMaxY() + by;
                        double bMinZ = blockBox.getMinZ() + bz, bMaxZ = blockBox.getMaxZ() + bz;

                        boolean overlap = bMaxX > minX && bMinX < maxX
                                && bMaxY > y && bMinY < maxY
                                && bMaxZ > minZ && bMinZ < maxZ;

                        if(overlap)
                            return false;
                    }
                }
            }
        }

        return true;
    }

    private static NPC rayTraceNpcs(Location start, Vector direction, double maxDistance)
    {
        NPC closestNpc = null;
        double closestDistance = maxDistance;

        for(NPC npc : NpcManager.getList())
        {
            if(!npc.getLocation().getWorld().equals(start.getWorld()))
                continue;

            WrappedEntity.BoundingBox box = npc.entity.getBoundingBox();
            double scale = npc.getOption(NpcOption.SCALE);
            double width = box.getXSize() * scale;
            double height = box.getYSize() * scale;

            Location loc = npc.getLocation();
            double halfWidth = width / 2.0;

            BoundingBox npcBox = new BoundingBox(loc.getX() - halfWidth, loc.getY(), loc.getZ() - halfWidth, loc.getX() + halfWidth,
                    loc.getY() + height, loc.getZ() + halfWidth);

            RayTraceResult result = npcBox.rayTrace(start.toVector(), direction, maxDistance);
            if(result != null)
            {
                double hitDist = result.getHitPosition().distance(start.toVector());
                if(hitDist < closestDistance)
                {
                    closestDistance = hitDist;
                    closestNpc = npc;
                }
            }
        }

        return closestNpc;
    }

    private static void handleProjectileHit(NPC npc, Projectile projectile)
    {
        if(!(projectile.getShooter() instanceof Player attacker))
            return;

        if(!npc.getCombatManager().isEnabled())
            return;

        CombatState state = states.computeIfAbsent(npc, n -> new CombatState());
        int currentTick = WrappedMinecraftServer.getCurrentTick();

        if(currentTick < state.invulnerableUntilTick)
            return;

        boolean isCrit = false;
        double baseDamage = 2.0;

        if(projectile instanceof AbstractArrow arrow)
        {
            isCrit = arrow.isCritical();
            baseDamage = Math.max(1.0, arrow.getVelocity().length() * arrow.getDamage());
        }
        else if(projectile instanceof Trident)
            baseDamage = 8.0;

        NpcDamageEvent damageEvent = new NpcDamageEvent(npc, attacker, baseDamage, isCrit, false);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if(damageEvent.isCancelled())
            return;

        double damage = damageEvent.getDamage();
        NpcCombatManager combatManager = npc.getCombatManager();
        double currentHealth = combatManager.getCurrentHealth();

        double newHealth = combatManager.isInvulnerable() ? currentHealth : currentHealth - damage;
        boolean dying = newHealth <= 0 && !combatManager.isInvulnerable();

        if(dying)
        {
            NpcDeathEvent deathEvent = new NpcDeathEvent(npc, attacker);
            Bukkit.getPluginManager().callEvent(deathEvent);

            if(deathEvent.isCancelled())
            {
                dying = false;
                newHealth = 1.0;
            }
        }

        attacker.playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
        attacker.playSound(attacker.getLocation(), dying ? Sound.ENTITY_PLAYER_DEATH : Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);

        npc.playAnimation(attacker, AnimatePacket.Animation.HURT);
        if(isCrit)
            npc.playAnimation(attacker, AnimatePacket.Animation.CRITICAL_HIT);

        state.invulnerableUntilTick = currentTick + INVULNERABLE_DURATION_TICKS;

        if(dying)
        {
            states.remove(npc);
            combatManager.setCurrentHealth(0, npc);
        }
        else
        {
            applyProjectileKnockback(npc, projectile.getVelocity(), state, combatManager.getKnockbackMultiplier());
            combatManager.setCurrentHealth(newHealth, npc);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        Projectile projectile = event.getEntity();

        if(projectile.getShooter() instanceof Player)
            activeProjectiles.put(projectile, projectile.getLocation());
    }

    @EventHandler
    public void onNpcHit(NpcInteractEvent event)
    {
        if(event.getAction() != ClickActionType.LEFT)
            return;

        if(event.isCancelled())
            return;

        NPC npc = event.getNpc();
        Player attacker = event.getPlayer();

        if(!npc.getCombatManager().isEnabled())
            return;

        boolean isCrit = attacker.getFallDistance() > 0.0F
                && !((Entity) attacker).isOnGround()
                && !attacker.isClimbing()
                && !attacker.isInWater()
                && !attacker.hasPotionEffect(PotionEffectType.BLINDNESS)
                && !attacker.isInsideVehicle();
        boolean isMagic = isMagicHit(attacker, npc);

        DamageResult incoming = computeIncomingDamage(attacker, npc, isCrit);

        NpcDamageEvent damageEvent = new NpcDamageEvent(npc, attacker, incoming.damage(), isCrit, isMagic);
        Bukkit.getPluginManager().callEvent(damageEvent);
        if(damageEvent.isCancelled())
            return;

        double damage = damageEvent.getDamage();

        CombatState state = states.computeIfAbsent(npc, n -> new CombatState());
        int currentTick = WrappedMinecraftServer.getCurrentTick();

        if(currentTick < state.invulnerableUntilTick)
        {
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0F, 1.0F);
            return;
        }

        state.lastHurt = damage;
        state.invulnerableUntilTick = currentTick + INVULNERABLE_DURATION_TICKS;

        NpcCombatManager combatManager = npc.getCombatManager();
        double currentHealth = combatManager.getCurrentHealth();

        double newHealth = combatManager.isInvulnerable() ? currentHealth : currentHealth - damage;
        boolean dying = newHealth <= 0 && !combatManager.isInvulnerable();

        if(dying)
        {
            NpcDeathEvent deathEvent = new NpcDeathEvent(npc, attacker);
            Bukkit.getPluginManager().callEvent(deathEvent);

            if(deathEvent.isCancelled())
            {
                dying = false;
                newHealth = 1.0;
            }
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        boolean hasKnockbackEnchant = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK) > 0;
        boolean isSprintingHit = attacker.isSprinting();
        boolean isKnockbackHit = isSprintingHit || hasKnockbackEnchant;
        boolean isSword = EnchantmentTarget.WEAPON.includes(weapon.getType());
        boolean isSweepHit = isSword && incoming.attackStrength > 0.9F && ((Entity) attacker).isOnGround() && !isSprintingHit;

        Sound attackSound;
        if(incoming.attackStrength <= 0.2F)
            attackSound = Sound.ENTITY_PLAYER_ATTACK_WEAK;
        else if(isCrit)
            attackSound = Sound.ENTITY_PLAYER_ATTACK_CRIT;
        else if(isKnockbackHit)
            attackSound = Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK;
        else if(isSweepHit)
            attackSound = Sound.ENTITY_PLAYER_ATTACK_SWEEP;
        else
            attackSound = Sound.ENTITY_PLAYER_ATTACK_STRONG;

        attacker.playSound(attacker.getLocation(), dying ? Sound.ENTITY_PLAYER_DEATH : Sound.ENTITY_PLAYER_HURT, 1f, 1f);
        attacker.playSound(attacker.getLocation(), attackSound, 1.0F, 1.0F);

        npc.playAnimation(attacker, AnimatePacket.Animation.HURT);

        if(isCrit)
            npc.playAnimation(attacker, AnimatePacket.Animation.CRITICAL_HIT);

        if(isMagic)
            npc.playAnimation(attacker, AnimatePacket.Animation.MAGIC_CRITICAL_HIT);

        applyWeaponDurability(attacker, weapon);

        if(dying)
        {
            states.remove(npc);
            combatManager.setCurrentHealth(0, npc);
        }
        else
        {
            applyKnockback(npc, attacker, state, incoming.knockbackResistance(), combatManager.getKnockbackMultiplier());
            combatManager.setCurrentHealth(newHealth, npc);
        }
    }

    private void applyKnockback(NPC npc, Player attacker, CombatState state, float knockbackResistance, double knockbackMultiplier)
    {
        if(knockbackMultiplier <= 0)
            return;

        Vector direction = npc.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
        if(direction.lengthSquared() < 1.0E-4)
            direction = npc.getLocation().getDirection().setY(0);

        direction.normalize();

        double power = KNOCKBACK_POWER * (1.0 - Math.clamp(knockbackResistance, 0.0F, 1.0F)) * knockbackMultiplier;

        state.vx = state.vx / 2.0 + direction.getX() * power;
        state.vz = state.vz / 2.0 + direction.getZ() * power;
        state.vy = state.grounded ? Math.min(0.4, state.vy / 2.0 + power) : state.vy;

        if(state.task == null)
            startPhysicsTask(npc, state);
    }

    private DamageResult computeIncomingDamage(Player attacker, NPC npc, boolean isCrit)
    {
        AttributeInstance attackDamage;
        try
        {
            attackDamage = attacker.getAttribute(Attribute.valueOf("ATTACK_DAMAGE"));
        }
        catch(Exception e)
        {
            attackDamage = attacker.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        }

        float attackStrength = getPlayerAttackStrength(attacker);
        lastAttackTicks.put(attacker.getUniqueId(), WrappedMinecraftServer.getCurrentTick());

        float damage = (attackDamage != null ? (float) attackDamage.getValue() : FALLBACK_DAMAGE);
        damage *= (0.2F + attackStrength * attackStrength * 0.8F);

        int sharpness = attacker.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SHARPNESS);
        if(sharpness > 0)
            damage += (sharpness * 0.5F + 0.5F) * attackStrength;

        if(isCrit)
            damage *= 1.5F;

        Map<EquipmentSlot, ItemStack> equipment = npc.getOption(NpcOption.EQUIPMENT);
        float knockbackResistance = 0F;

        if(equipment != null && !equipment.isEmpty())
        {
            ArmorStats armor = sumArmorStats(equipment);
            damage = applyArmorAbsorption(damage, armor.points(), armor.toughness());
            knockbackResistance = armor.knockbackResistance();

            if(armor.protectionLevel() > 0)
            {
                float reduction = Math.min(armor.protectionLevel() * 0.04F, 0.8F);
                damage *= 1.0F - reduction;
            }
        }

        return new DamageResult(Math.max(damage, 0F), knockbackResistance, attackStrength);
    }

    private float getPlayerAttackStrength(Player player)
    {
        int currentTick = WrappedMinecraftServer.getCurrentTick();
        int lastTick = lastAttackTicks.getOrDefault(player.getUniqueId(), currentTick - 100);

        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        double attackSpeed = (speedAttr != null) ? speedAttr.getValue() : 4.0;

        float cooldownPeriod = (float) (20.0 / attackSpeed);
        float elapsedTicks = (currentTick - lastTick) + 0.5F;

        return Math.clamp(elapsedTicks / cooldownPeriod, 0.0F, 1.0F);
    }

    private float applyArmorAbsorption(float damage, float armor, float toughness)
    {
        float f = 2.0F + toughness / 4.0F;
        float clampedArmor = Math.clamp(armor - damage / f, armor * 0.2F, 20.0F);
        return damage * (1.0F - clampedArmor / 25.0F);
    }

    private ArmorStats sumArmorStats(Map<EquipmentSlot, ItemStack> equipment)
    {
        float points = 0;
        float toughness = 0;
        float knockbackResistance = 0;
        int protection = 0;

        EquipmentSlot[] armorSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for(EquipmentSlot slot : armorSlots)
        {
            ItemStack item = equipment.get(slot);
            if(item == null || item.getType() == Material.AIR)
                continue;

            ItemMeta meta = item.getItemMeta();

            Multimap<Attribute, AttributeModifier> modifiers = (meta != null && meta.hasAttributeModifiers())
                    ? meta.getAttributeModifiers(slot)
                    : item.getType().getDefaultAttributeModifiers(slot);

            if(modifiers != null)
            {
                for(AttributeModifier mod : modifiers.get(Attribute.GENERIC_ARMOR))
                    points += (float) mod.getAmount();
                for(AttributeModifier mod : modifiers.get(Attribute.GENERIC_ARMOR_TOUGHNESS))
                    toughness += (float) mod.getAmount();
                for(AttributeModifier mod : modifiers.get(Attribute.GENERIC_KNOCKBACK_RESISTANCE))
                    knockbackResistance += (float) mod.getAmount();
            }

            protection += item.getEnchantmentLevel(Enchantment.PROTECTION);
        }

        return new ArmorStats(points, toughness, knockbackResistance, protection);
    }

    private boolean isMagicHit(Player attacker, NPC npc)
    {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if(weapon == null || weapon.getType() == Material.AIR)
            return false;

        if(weapon.getEnchantmentLevel(Enchantment.SHARPNESS) > 0)
            return true;

        EntityType entityType = npc.entity.getBukkitPlayer().getType();

        if(weapon.getEnchantmentLevel(Enchantment.SMITE) > 0 && isUndead(entityType))
            return true;

        if(weapon.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS) > 0 && isArthropod(entityType))
            return true;

        if(weapon.getEnchantmentLevel(Enchantment.IMPALING) > 0 && isAquatic(entityType))
            return true;

        return false;
    }

    private boolean isUndead(EntityType type)
    {
        return switch(type)
        {
            case ZOMBIE, SKELETON, WITHER, WITHER_SKELETON, ZOMBIFIED_PIGLIN,
                 ZOGLIN, PHANTOM, DROWNED, HUSK, STRAY, ZOMBIE_VILLAGER -> true;
            default -> false;
        };
    }

    private boolean isArthropod(EntityType type)
    {
        return switch(type)
        {
            case SPIDER, CAVE_SPIDER, SILVERFISH, ENDERMITE, BEE -> true;
            default -> false;
        };
    }

    private boolean isAquatic(EntityType type)
    {
        return switch(type)
        {
            case GUARDIAN, ELDER_GUARDIAN, SQUID, GLOW_SQUID, TURTLE, COD, SALMON, PUFFERFISH, TROPICAL_FISH -> true;
            default -> false;
        };
    }

    private void applyWeaponDurability(Player player, ItemStack weapon)
    {
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        if (weapon == null || weapon.getType() == Material.AIR)
            return;

        if (weapon.getType().getMaxDurability() > 0 && weapon.getItemMeta() instanceof Damageable damageable)
        {
            int unbreakingLevel = weapon.getEnchantmentLevel(Enchantment.UNBREAKING);

            Object component = Var.getComponent(Var.toNmsItemStack(weapon), Var.DataComponents.WEAPON);

            int baseDurabilityLoss;
            if(!Versions.isCurrentVersionSmallerThan(Versions.V1_21_5))
            {
                try
                {
                    baseDurabilityLoss = (int) Reflections.invokeMethod(component, "a").get();
                }
                catch(Exception e)
                {
                    baseDurabilityLoss = (int) Reflections.invokeMethod(component, "itemDamagePerAttack").get();
                }
            }
            else
                baseDurabilityLoss = EnchantmentTarget.TOOL.includes(weapon) ? 2 : 1;

            int durabilityToReduce = 0;
            for (int i = 0; i < baseDurabilityLoss; i++)
            {
                if (ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0)
                    durabilityToReduce++;
            }

            if (durabilityToReduce > 0)
            {
                int newDamage = damageable.getDamage() + durabilityToReduce;

                if (newDamage >= weapon.getType().getMaxDurability())
                {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                    player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                }
                else
                {
                    damageable.setDamage(newDamage);
                    weapon.setItemMeta(damageable);
                }
            }
        }
    }

    private record DamageResult(float damage, float knockbackResistance, float attackStrength) {}

    private record ArmorStats(float points, float toughness, float knockbackResistance, int protectionLevel) {}

    private static class CombatState
    {
        double vx, vy, vz;
        boolean grounded = true;
        BukkitTask task;

        double lastHurt = -1;
        int invulnerableUntilTick = 0;
    }
}