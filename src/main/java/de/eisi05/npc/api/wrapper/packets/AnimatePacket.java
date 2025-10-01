package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_9), path = "net.minecraft.network.protocol.game.PacketPlayOutAnimation")
public class AnimatePacket extends PacketWrapper
{
    protected AnimatePacket(@NotNull WrappedServerPlayer player, int animationId)
    {
        super(AnimatePacket.class, player, animationId);
    }

    public static PacketWrapper create(@NotNull WrappedServerPlayer player, @NotNull Animation animation)
    {
        if(animation != Animation.HURT || Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
            return new AnimatePacket(player, animation.ordinal());

        return new HurtAnimationPacket(player);
    }

    public enum Animation implements Serializable
    {
        SWING_MAIN_HAND,
        HURT,
        WAKE_UP,
        SWING_OFF_HAND,
        CRITICAL_HIT,
        MAGIC_CRITICAL_HIT
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_9),
            path = "net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket")
    public static class HurtAnimationPacket extends PacketWrapper
    {
        private HurtAnimationPacket(@NotNull WrappedServerPlayer player)
        {
            super(HurtAnimationPacket.class, player);
        }
    }
}
