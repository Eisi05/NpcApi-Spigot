package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayOutAnimation")
public class AnimatePacket extends PacketWrapper
{
    protected AnimatePacket(@NotNull WrappedEntity<?> entity, int animationId)
    {
        super(AnimatePacket.class, entity, animationId);
    }

    public static @Nullable PacketWrapper create(@NotNull WrappedEntity<?> entity, @NotNull Animation animation)
    {
        if(animation != Animation.HURT || Versions.isCurrentVersionSmallerThan(Versions.V1_19_4))
            return new AnimatePacket(entity, animation.ordinal());

        if(!(entity.getBukkitPlayer() instanceof LivingEntity))
            return null;

        return new HurtAnimationPacket(entity);
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

    @Mapping(range = @Mapping.Range(from = Versions.V1_19_4, to = Versions.V1_21_11),
            path = "net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket")
    public static class HurtAnimationPacket extends PacketWrapper
    {
        private HurtAnimationPacket(@NotNull WrappedEntity<?> entity)
        {
            super(HurtAnimationPacket.class, entity);
        }
    }
}
