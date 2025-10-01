package de.eisi05.npc.api.wrapper.enums;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.EnumChatFormat")
public enum ChatFormat implements Wrapper.EnumWrapper, Serializable
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "a")
    BLACK(ChatColor.BLACK),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "b")
    DARK_BLUE(ChatColor.DARK_BLUE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "c")
    DARK_GREEN(ChatColor.DARK_GREEN),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "d")
    DARK_AQUA(ChatColor.DARK_AQUA),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "e")
    DARK_RED(ChatColor.DARK_RED),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "f")
    DARK_PURPLE(ChatColor.DARK_PURPLE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "g")
    GOLD(ChatColor.GOLD),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "h")
    GRAY(ChatColor.GRAY),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "i")
    DARK_GRAY(ChatColor.DARK_GRAY),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "j")
    BLUE(ChatColor.BLUE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "k")
    GREEN(ChatColor.GREEN),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "l")
    AQUA(ChatColor.AQUA),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "m")
    RED(ChatColor.RED),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "n")
    LIGHT_PURPLE(ChatColor.LIGHT_PURPLE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "o")
    YELLOW(ChatColor.YELLOW),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "p")
    WHITE(ChatColor.WHITE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "q")
    OBFUSCATED(ChatColor.MAGIC),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "r")
    BOLD(ChatColor.BOLD),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "s")
    STRIKETHROUGH(ChatColor.STRIKETHROUGH),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "t")
    UNDERLINE(ChatColor.UNDERLINE),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "u")
    ITALIC(ChatColor.ITALIC),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "v")
    RESET(ChatColor.RESET);

    private final ChatColor color;

    ChatFormat(@NotNull ChatColor color)
    {
        this.color = color;
    }

    public static @Nullable ChatFormat fromChatColor(@NotNull ChatColor color)
    {
        for(ChatFormat format : ChatFormat.values())
        {
            if(format.color.equals(color))
                return format;
        }
        return null;
    }

    public ChatColor getBukkitColor()
    {
        return color;
    }

    @Override
    public @NotNull Object getHandle()
    {
        return cast(this);
    }
}
