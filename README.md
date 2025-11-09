[![](https://jitpack.io/v/Eisi05/NpcApi.svg)](https://jitpack.io/#Eisi05/NpcApi)

[NPC Plugin on SpigotMC](https://www.spigotmc.org/resources/npc-plugin-1-17.87761/)

[NpcApi for PaperMC](https://github.com/Eisi05/NpcApi-Paper)

# NpcAPI

A powerful and easy-to-use NPC (Non-Player Character) API for Minecraft Spigot plugins that allows you to create, manage, and customize NPCs with
advanced features.

## Features

- Create custom NPCs with ease
- Customize NPC appearance (skins, glowing effects, etc.)
- Handle click events and interactions
- Play animations and control NPC behavior
- Save and load NPCs persistently
- Show/hide NPCs for specific players
- Comprehensive NPC management system
- Let NPCs walk around

## Installation
Choose your preferred installation method based on your project needs:

### Method 1: Plugin Dependency (Recommended)

This method requires [NpcPlugin](https://www.spigotmc.org/resources/npc-plugin-1-17.87761/) to be installed as a separate plugin on the server.

#### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Eisi05</groupId>
    <artifactId>NpcApi</artifactId>
    <version>1.2.6</version>
    <scope>provided</scope>
</dependency>
```

#### Gradle
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compileOnly 'com.github.Eisi05:NpcApi:1.2.6'
}
```

#### Plugin Configuration
Add NpcPlugin as a dependency in your `plugin.yml`:
```yaml
# Required dependency (hard dependency)
depend: [NpcPlugin]

# Or optional dependency (soft dependency)
soft-depend: [NpcPlugin]
```
---

### Method 2: Shaded Dependency

This method bundles NpcApi directly into your plugin JAR file.

#### Maven
Add the repository and dependency to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Eisi05</groupId>
        <artifactId>NpcApi</artifactId>
        <version>1.2.6</version>
    </dependency>
</dependencies>
```

#### Gradle
Add the following to your `build.gradle`:
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Eisi05:NpcApi:1.2.6'
}
```

#### Plugin Configuration
To enabled/disable the NpcApi add this to your Plugin Main class:
```java

@Override
public void onEnable()
{
    // Initialize NpcAPI with default configuration
    NpcApi.createInstance(this, new NpcConfig());
}

@Override
public void onDisable()
{
    // Properly disable NpcAPI
    NpcApi.disable();
}
```
---

## Usage Examples

### Creating a Basic NPC

```java
// Create a location where the NPC should spawn
Location location = new Location(world, x, y, z);

// Create a new NPC with a name
NPC npc = new NPC(location, WrappedComponent.create("Test"));

// Enable the NPC to make it visible to all players
npc.setEnabled(true);
```

### Customizing NPC Appearance

```java
// Make the NPC glow with a red color
npc.setOption(NpcOption.GLOWING, ChatColor.RED);

// Set a custom skin from a player
npc.setOption(NpcOption.SKIN, NpcSkin.of(Skin.fromPlayer(player)));
```

### Handling Click Events

```java
// Set up a click event handler
npc.setClickEvent(event -> {
    Player player = event.getPlayer();
    NPC clickedNpc = event.getNpc();
    player.sendMessage("You clicked " + clickedNpc.getName().toLegacy());
});
```

### Managing NPC State

```java
npc.save();
npc.setName(NpcName.of(WrappedComponent.create("New Name")));
npc.setLocation(newLocation);
npc.reload();
```

### Advanced NPC Control

```java
npc.playAnimation(/* animation parameters */);
npc.showNPCToPlayer(player);
npc.hideNpcFromPlayer(player);
npc.lookAtPlayer(player);
npc.delete();
npc.walkTo(path, player, walkSpeed, changeRealLocation);
```

## NPC Management

### Getting All NPCs

```java
// Get a list of all available NPCs
List<NPC> allNpcs = NpcManager.getList();
```

### Finding NPCs by UUID

```java
// Get a specific NPC by its UUID
UUID npcUuid = /* your NPC's UUID */;
NPC npc = NpcManager.fromUUID(npcUuid);
```

## API Reference

### NPC Class

| Method                                  | Description                                                                 |
|-----------------------------------------|-----------------------------------------------------------------------------|
| `setOption(NpcOption, Object)`          | Set NPC options like glowing, skin, etc.                                    |
| `setClickEvent(Consumer<ClickEvent>)`   | Set the click event handler                                                 |
| `setEnabled(boolean)`                   | Enable/disable NPC visibility                                               |
| `save()`                                | Save NPC to persistent storage                                              |
| `reload()`                              | Reload NPC data                                                             |
| `setName(NpcName)`                      | Update NPC display name                                                     |
| `setLocation(Location)`                 | Move NPC to new location                                                    |
| `playAnimation(...)`                    | Play NPC animation                                                          |
| `showNPCToPlayer(Player)`               | Show NPC to specific player                                                 |
| `hideNpcFromPlayer(Player)`             | Hide NPC from specific player                                               |
| `lookAtPlayer(Player)`                  | Make NPC look at player                                                     |
| `delete()`                              | Remove NPC permanently                                                      |
| `walkTo(Path, player, double, boolean, Consumer<Result>)` | Let the NPC walk along a path (can be created with PathfindingUtils class)  |

## Requirements

- Java 17+
- Spigot 1.17+ (compatible with newer versions)
- Minecraft server with NPC support
