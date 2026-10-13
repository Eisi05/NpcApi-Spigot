package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.Difficulty;
import org.jetbrains.annotations.NotNull;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayOutServerDifficulty")
public class DifficultyPacket extends PacketWrapper.PacketHolder
{
    protected DifficultyPacket(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "difficulty")
    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "b")
    public @NotNull Difficulty getDifficulty()
    {
        Enum<?> enumValue = getWrappedFieldValue();
        return Difficulty.values()[enumValue.ordinal()];
    }
}
