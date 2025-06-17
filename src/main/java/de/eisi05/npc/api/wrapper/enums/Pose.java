package de.eisi05.npc.api.wrapper.enums;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.world.entity.EntityPose")
public enum Pose implements Wrapper.EnumWrapper
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "a")
    STANDING(getBukkit("STANDING"), Material.ARMOR_STAND),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "b")
    FALL_FLYING(getBukkit("FALL_FLYING"), Material.FEATHER),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "c")
    SLEEPING(getBukkit("SLEEPING"), Material.RED_BED),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "d")
    SWIMMING(getBukkit("SWIMMING"), Material.WATER_BUCKET),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "e")
    SPIN_ATTACK(getBukkit("SPIN_ATTACK"), Material.TRIDENT),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "f")
    CROUCHING(getBukkit("SNEAKING"), Material.LEATHER_BOOTS),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "g")
    LONG_JUMPING(getBukkit("LONG_JUMPING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "h")
    DYING(getBukkit("DYING"), Material.ROTTEN_FLESH),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_6), path = "i")
    CROAKING(getBukkit("CROAKING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_21_6), path = "j")
    USING_TONGUE(getBukkit("USING_TONGUE"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "k")
    SITTING(getBukkit("SITTING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "l")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "k")
    ROARING(getBukkit("ROARING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "m")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "l")
    SNIFFING(getBukkit("SNIFFING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "n")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "m")
    EMERGING(getBukkit("EMERGING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_3, to = Versions.V1_21_6), path = "o")
    @Mapping(range = @Mapping.Range(from = Versions.V1_19, to = Versions.V1_19_1), path = "n")
    DIGGING(getBukkit("DIGGING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_6), path = "p")
    SLIDING(getBukkit("SLIDING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_6), path = "q")
    SHOOTING(getBukkit("SHOOTING"), null),

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_4, to = Versions.V1_21_6), path = "r")
    INHALING(getBukkit("INHALING"), null);

    private final org.bukkit.entity.Pose bukkitPose;
    private final Material icon;

    Pose(@Nullable org.bukkit.entity.Pose bukkitPose, Material icon)
    {
        this.bukkitPose = bukkitPose;
        this.icon = icon;
    }

    public static @Nullable Pose fromBukkit(@NotNull org.bukkit.entity.Pose bukkitPose)
    {
        for(Pose pose : Pose.values())
        {
            if(pose.bukkitPose == null)
                continue;

            if(pose.bukkitPose == bukkitPose)
                return pose;
        }
        return null;
    }

    private static @Nullable org.bukkit.entity.Pose getBukkit(@NotNull String pose)
    {
        try
        {
            return org.bukkit.entity.Pose.valueOf(pose);
        } catch(IllegalArgumentException e)
        {
            return null;
        }
    }

    public static @NotNull Pose[] getValues()
    {
        return Arrays.stream(values()).filter(pose -> pose.bukkitPose != null).toArray(Pose[]::new);
    }

    public org.bukkit.entity.Pose toBukkit()
    {
        return bukkitPose;
    }

    public boolean isForPlayer()
    {
        return icon != null;
    }

    public Material getIcon()
    {
        return icon;
    }

    @Override
    public @NotNull Object getHandle()
    {
        return cast(this);
    }
}
