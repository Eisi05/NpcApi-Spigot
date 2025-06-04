package de.eisi05.npc.api.wrapper.packets;

import com.google.common.collect.ImmutableList;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.objects.WrappedPlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
        ".PacketPlayOutScoreboardTeam")
public class SetPlayerTeamPacket extends PacketWrapper
{
    private SetPlayerTeamPacket(@NotNull String teamName, int method, @NotNull Optional<Object> parameter, @NotNull Collection<String> players)
    {
        super(SetPlayerTeamPacket.class, teamName, method, parameter, players);
    }

    public static SetPlayerTeamPacket createAddOrModifyPacket(@NotNull WrappedPlayerTeam team, boolean create)
    {
        return new SetPlayerTeamPacket(team.getName(), create ? 0 : 2, Optional.of(new Parameters(team).getHandle()),
                (create ? team.getPlayers() : ImmutableList.of()));
    }

    public static SetPlayerTeamPacket createRemovePacket(@NotNull WrappedPlayerTeam team)
    {
        return new SetPlayerTeamPacket(team.getName(), 1, Optional.empty(), ImmutableList.of());
    }

    public static SetPlayerTeamPacket createPlayerPacket(@NotNull WrappedPlayerTeam team, @NotNull String playerName, @NotNull Action action)
    {
        return new SetPlayerTeamPacket(team.getName(), action == Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.of(playerName));
    }

    public enum Action
    {
        ADD,
        REMOVE
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_5), path = "net.minecraft.network.protocol.game" +
            ".PacketPlayOutScoreboardTeam$b")
    public static class Parameters extends Wrapper
    {
        public Parameters(@NotNull WrappedPlayerTeam team)
        {
            super(createInstance(Parameters.class, team.getHandle()));
        }
    }
}
