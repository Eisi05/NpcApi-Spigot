# NPC AI Goal System

The NPC API now includes a flexible goal-based AI system similar to Minecraft's entity goal system. This allows NPCs to have autonomous behaviors that can be combined and prioritized.

## Architecture

### Goal Abstract Class
All goals extend the `Goal` abstract class with the following methods:
- `canUse(NPC)` - Whether this goal can be used right now
- `start(NPC)` - Called when the goal starts executing
- `tick(NPC)` - Called each tick while the goal is active
- `stop(NPC)` - Called when the goal stops executing
- `getPriority()` - Returns the Priority enum of this goal
- `canContinue(NPC)` - Whether this goal can continue executing

### GoalSelector
The `GoalSelector` manages and selects which goal to execute based on priority and conditions. It evaluates all goals each tick and selects a goal based on the priority system.

## Available Goals

### 1. WalkToLocationGoal
Makes the NPC walk to a specific location using pathfinding.

```java
Location target = new Location(world, 100, 64, 100);
WalkToLocationGoal walkGoal = new WalkToLocationGoal.Builder(target)
    .speed(0.4)
    .build();

// With custom settings
WalkToLocationGoal walkGoal = new WalkToLocationGoal.Builder(targetLocation)
    .speed(0.4)                    // speed (0.1-1.0)
    .maxIterations(5000)          // max pathfinding iterations
    .allowDiagonal(true)          // allow diagonal movement
    .withRotation(true)           // include rotation packets
    .completionCallback(result -> { // completion callback
        if(result == WalkingResult.SUCCESS) {
            Bukkit.broadcastMessage("NPC reached destination!");
        }
    })
    .build();
```

### 2. AttackEntityGoal
Makes the NPC attack nearby entities that match a predicate. Behavior varies based on held item:
- Bow/Crossbow/Trident: Long range (15 blocks)
- Sword/Axe/Other: Short range (3 blocks)
- Only activates when target is in line of sight
- Dynamically discovers targets within range

```java
// Attack all players
AttackEntityGoal attackGoal = new AttackEntityGoal(entity -> entity instanceof Player);

// Attack low health players
AttackEntityGoal attackGoal = new AttackEntityGoal(
    entity -> entity instanceof Player && ((Player) entity).getHealth() < 10
);

// With custom attack range
AttackEntityGoal attackGoal = new AttackEntityGoal(
    entity -> entity instanceof Monster,
    15.0  // custom attack range
);
```

### 3. LookAroundGoal
Makes the NPC look around randomly. Can serve as an idle/wait behavior.

```java
// Default settings (1-4 seconds)
LookAroundGoal lookGoal = new LookAroundGoal();

// Custom duration
LookAroundGoal lookGoal = new LookAroundGoal(40, 200); // 2-10 seconds
```

### 4. WaitGoal
Makes the NPC wait/idle for a specified duration.

```java
// Wait for 5 seconds (100 ticks)
WaitGoal waitGoal = new WaitGoal(100);
```

### 5. WanderGoal
Makes the NPC wander randomly to nearby locations.

```java
// Default settings (10 block radius)
WanderGoal wanderGoal = new WanderGoal();

// Custom radius and speed
WanderGoal wanderGoal = new WanderGoal(15, 60, 200, 0.3);
```

### 6. FollowEntityGoal
Makes the NPC follow a target entity by UUID, maintaining a specified distance.

```java
// Default settings (10 block follow distance, 1.5 block stop distance)
FollowEntityGoal followGoal = new FollowEntityGoal(playerToFollow.getUniqueId());

// Custom settings
FollowEntityGoal followGoal = new FollowEntityGoal(
    playerToFollow.getUniqueId(),
    5.0,    // follow distance
    2.0,    // stop distance
    0.5     // speed
);
```

## Usage Examples

### Basic Setup
```java
// Create NPC
NPC npc = new NPC(location);

// Add goals
npc.addGoal(new WanderGoal(10));
npc.addGoal(new LookAroundGoal());
npc.addGoal(new WalkToLocationGoal.Builder(target).build());

// Start goal system (goals auto-save when added)
npc.startGoals();
```

### Guard NPC Example
```java
NPC guard = new NPC(spawnLocation);

// Equip with sword
Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
equipment.put(EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));
guard.setOption(NpcOption.EQUIPMENT, equipment);

// Add goals - attack has ALWAYS priority, others are randomizable
guard.addGoal(new AttackEntityGoal(entity -> entity instanceof Monster));
guard.addGoal(new WanderGoal(8));
guard.addGoal(new LookAroundGoal());

guard.startGoals();
```

### Following Player Example
```java
NPC companion = new NPC(player.getLocation());

companion.addGoal(new FollowEntityGoal(player.getUniqueId(), 5.0, 2.0, 0.4));
companion.addGoal(new LookAroundGoal());

companion.startGoals();
```

### Manual Goal Control
```java
NPC npc = new NPC(location);

// Add goals
npc.addGoal(new WanderGoal());
npc.addGoal(new AttackEntityGoal(entity -> entity instanceof Player));

// Start system
npc.startGoals();

// Stop system
npc.stopGoals();
```

## Priority System

Goals use a priority-based selection system with the following levels:

- **ALWAYS** - Always preferred over other goals and selected deterministically (e.g., AttackEntityGoal, FollowEntityGoal)
- **MID_CHANCE** - Medium chance of being selected when among non-ALWAYS goals (e.g., WanderGoal, WalkToLocationGoal)
- **LOW_CHANCE** - Low chance of being selected when among non-ALWAYS goals (e.g., LookAroundGoal, WaitGoal)

The goal selector evaluates all goals each tick:
1. If any goal has `ALWAYS` priority, one is randomly selected from those
2. Otherwise, weighted random selection is used among available goals based on their priority weights (higher weight = higher chance)

## Best Practices

1. **Goals auto-save**: Goals are automatically saved when added or removed via `addGoal()` and `removeGoal()`. No manual saving required.

2. **Goals auto-start**: The goal system automatically starts when an NPC is loaded from disk if it has saved goals.

3. **Use predicates for target filtering**: AttackEntityGoal uses predicates to dynamically discover targets within range, making it more flexible than fixed target suppliers.

4. **Combine goals strategically**: Use a mix of ALWAYS priority reactive goals (like AttackEntityGoal) and randomizable idle goals (like WanderGoal, LookAroundGoal).

5. **Set appropriate priorities**: Use ALWAYS for critical behaviors that should always execute (like combat), and use HIGH/MID/LOW_CHANCE for behaviors that can be randomly selected.

6. **Test pathfinding**: The WalkToLocationGoal uses A* pathfinding which can be CPU-intensive. Adjust `maxIterations` based on your server's performance.

## Notes

- The AStarPathfinder is suitable for constant movement, but for very frequent pathfinding, consider caching paths or using simpler movement for short distances.
- Goals run on the main server thread, so avoid heavy computations in `canUse()` or `tick()`.
- Goals implement `Serializable` and are saved with the NPC. Transient fields (like entity references) are handled via custom serialization.
- The goal selector runs every tick by default. Adjust with `npc.getGoalSelector().setTickInterval(ticks)` (internal API).
- `getGoalSelector()` is now private - use the public goal management methods (`addGoal`, `removeGoal`, `startGoals`, `stopGoals`).
