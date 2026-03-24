package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.ChatFormat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.world.scores.PlayerTeam")
@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.scores.ScoreboardTeam")
public class WrappedPlayerTeam extends Wrapper
{
    private static final Map<UUID, Map<String, WrappedPlayerTeam>> teams = new HashMap<>();

    private WrappedPlayerTeam(Object handle)
    {
        super(handle);
    }

    public static @NotNull WrappedPlayerTeam getPlayersTeam(@NotNull Player player)
    {
        return new WrappedPlayerTeam(Reflections.invokeMethod(player.getScoreboard(), "getHandle")
                .thanInvoke(switch(Versions.getVersion())
                {
                    case NONE -> null;
                    case V1_17 -> "getPlayerTeam";
                    case V1_18, V1_18_2, V1_19, V1_19_1, V1_19_3, V1_19_4, V1_20 -> "i";
                    case V1_20_2 -> "g";
                    case V1_20_4, V1_20_6, V1_21, V1_21_2, V1_21_4, V1_21_5, V1_21_6, V1_21_9, V1_21_11 -> "e";
                    case V26_1 -> "getPlayersTeam";
                }, WrappedServerPlayer.fromPlayer(player).getName()).get());
    }

    public static @NotNull WrappedPlayerTeam create(@NotNull Player player, @NotNull String name)
    {
        if(exists(player, name))
            return teams.get(player.getUniqueId()).get(name);

        Object scoreboard = Reflections.invokeMethod(player.getScoreboard(), "getHandle").get();

        WrappedPlayerTeam wrappedPlayerTeam = createWrappedInstance(WrappedPlayerTeam.class, scoreboard, name);

        var map = teams.getOrDefault(player.getUniqueId(), new HashMap<>());
        map.put(name, wrappedPlayerTeam);
        teams.put(player.getUniqueId(), map);

        return wrappedPlayerTeam;
    }

    public static void clear()
    {
        teams.clear();
    }

    public static void clear(@NotNull UUID uuid)
    {
        teams.remove(uuid);
    }

    public static void clear(String name)
    {
        for(var entry : teams.entrySet())
            entry.getValue().keySet().removeIf(s -> s.equals(name));
    }

    public static void clear(UUID uuid, String name)
    {
        teams.get(uuid).remove(name);
    }

    public static boolean exists(@NotNull Player player, @NotNull String name)
    {
        return teams.getOrDefault(player.getUniqueId(), new HashMap<>()).containsKey(name);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setNameTagVisibility")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setNameTagVisibility")
    public void setNameTagVisibility(@NotNull Visibility visibility)
    {
        invokeWrappedMethod(visibility.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setCollisionRule")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setCollisionRule")
    public void setCollisionRule(@NotNull CollisionRule collisionRule)
    {
        invokeWrappedMethod(collisionRule.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setSeeFriendlyInvisibles")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setCanSeeFriendlyInvisibles")
    public void setCanSeeFriendlyInvisible(boolean canSeeFriendlyInvisible)
    {
        invokeWrappedMethod(canSeeFriendlyInvisible);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getPlayers")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_11), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_4), path = "g")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getPlayerNameSet")
    public @NotNull Collection<String> getPlayers()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "getName")
    @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_11), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_4), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getName")
    public @NotNull String getName()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setColor")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setColor")
    public void setColor(@NotNull ChatFormat color)
    {
        invokeWrappedMethod(color);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "setPlayerPrefix")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_11), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setPrefix")
    public void setPrefix(@NotNull WrappedComponent prefix)
    {
        invokeWrappedMethod(prefix);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.world.scores.Team$Visibility")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility")
    public enum Visibility implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "ALWAYS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        ALWAYS,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "NEVER")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "b")
        NEVER,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "HIDE_FOR_OTHER_TEAMS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "c")
        HIDE_FOR_OTHER_TEAMS,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "HIDE_FOR_OWN_TEAM")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "d")
        HIDE_FOR_OWN_TEAM;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "net.minecraft.world.scores.Team$CollisionRule")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush")
    public enum CollisionRule implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "ALWAYS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        ALWAYS,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "NEVER")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "b")
        NEVER,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "PUSH_OTHER_TEAMS")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "c")
        PUSH_OTHER_TEAMS,

        @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_1), path = "PUSH_OWN_TEAM")
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "d")
        PUSH_OWN_TEAM;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
