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

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.world.scores.ScoreboardTeam")
public class WrappedPlayerTeam extends Wrapper
{
    private static final Map<UUID, Map<String, WrappedPlayerTeam>> teams = new HashMap<>();

    private WrappedPlayerTeam(Object handle)
    {
        super(handle);
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

    public static boolean exists(@NotNull Player player, @NotNull String name)
    {
        return teams.getOrDefault(player.getUniqueId(), new HashMap<>()).containsKey(name);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_9), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setNameTagVisibility")
    public void setNameTagVisibility(@NotNull Visibility visibility)
    {
        invokeWrappedMethod(visibility.getHandle());
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_9), path = "h")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_4), path = "g")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getPlayerNameSet")
    public @NotNull Collection<String> getPlayers()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21_5, to = Versions.V1_21_9), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_4), path = "b")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "getName")
    public @NotNull String getName()
    {
        return invokeWrappedMethod();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_9), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setColor")
    public void setColor(@NotNull ChatFormat color)
    {
        invokeWrappedMethod(color);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_18, to = Versions.V1_21_9), path = "a")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_17), path = "setPrefix")
    public void setPrefix(@NotNull WrappedComponent prefix)
    {
        invokeWrappedMethod(prefix);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.world.scores" +
            ".ScoreboardTeamBase$EnumNameTagVisibility")
    public enum Visibility implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
        ALWAYS,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "b")
        NEVER,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "c")
        HIDE_FOR_OTHER_TEAMS,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "d")
        HIDE_FOR_OWN_TEAM;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
