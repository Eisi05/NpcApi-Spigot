package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.InteractionHand;
import de.eisi05.npc.api.wrapper.objects.WrappedEntity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "net.minecraft.network.protocol.game.ClientboundAnimatePacket")
@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayOutAnimation")
public class AnimatePacket extends PacketWrapper
{
    protected AnimatePacket(@NotNull WrappedEntity<?> entity, int animationId)
    {
        super(AnimatePacket.class, entity, animationId);
    }

    public static @Nullable PacketWrapper create(@NotNull WrappedEntity<?> entity, @NotNull Animation animation)
    {
        if(animation != Animation.HURT)
        {
            if(!Versions.isCurrentVersionSmallerThan(Versions.V26_3))
            {
                return switch(animation)
                {
                    case WAKE_UP -> new AnimatePacket(entity, 0);
                    case CRITICAL_HIT -> new AnimatePacket(entity, 1);
                    case MAGIC_CRITICAL_HIT -> new AnimatePacket(entity, 2);
                    case SWING_MAIN_HAND ->  new SwingAnimationPacket(entity.getId(), InteractionHand.MAIN_HAND,
                            new SwingAnimationPacket.SwingAnimation(SwingAnimationPacket.SwingAnimationType.WHACK,
                                    SwingAnimationPacket.SwingAnimation.DEFAULT_DURATION_WHACK));
                    case SWING_OFF_HAND -> new SwingAnimationPacket(entity.getId(), InteractionHand.OFF_HAND,
                            new SwingAnimationPacket.SwingAnimation(SwingAnimationPacket.SwingAnimationType.WHACK,
                                    SwingAnimationPacket.SwingAnimation.DEFAULT_DURATION_WHACK));
                    case STAB_MAIN_HAND -> new SwingAnimationPacket(entity.getId(), InteractionHand.MAIN_HAND,
                            new SwingAnimationPacket.SwingAnimation(SwingAnimationPacket.SwingAnimationType.STAB,
                                    SwingAnimationPacket.SwingAnimation.DEFAULT_DURATION_STAB));
                    case STAB_OFF_HAND -> new SwingAnimationPacket(entity.getId(), InteractionHand.OFF_HAND,
                            new SwingAnimationPacket.SwingAnimation(SwingAnimationPacket.SwingAnimationType.STAB,
                                    SwingAnimationPacket.SwingAnimation.DEFAULT_DURATION_STAB));
                    default -> null;
                };
            }

            return new AnimatePacket(entity, animation.ordinal());
        }

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
        MAGIC_CRITICAL_HIT,
        /**
         * @since 26.3
         */
        STAB_MAIN_HAND,
        /**
         * @since 26.3
         */
        STAB_OFF_HAND;

        public static Animation[] getCompatibleAnimations()
        {
            if(Versions.isCurrentVersionSmallerThan(Versions.V26_3))
                return Arrays.stream(values()).filter(animation -> animation != STAB_MAIN_HAND && animation != STAB_OFF_HAND).toArray(Animation[]::new);
            return values();
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_3),
            path = "net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket")
    public static class HurtAnimationPacket extends PacketWrapper
    {
        private HurtAnimationPacket(@NotNull WrappedEntity<?> entity)
        {
            super(HurtAnimationPacket.class, entity);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "net.minecraft.network.protocol.game.ClientboundSwingAnimationPacket")
    public static class SwingAnimationPacket extends PacketWrapper
    {
        protected SwingAnimationPacket(int entityId, InteractionHand hand, SwingAnimation animation)
        {
            super(createInstance(SwingAnimationPacket.class, entityId, hand.getHandle(), animation.getHandle()));
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "net.minecraft.world.item.component.SwingAnimation")
        public static class SwingAnimation extends Wrapper
        {
            private static final int DEFAULT_DURATION_WHACK = 6;
            private static final int DEFAULT_DURATION_STAB = 12;

            public SwingAnimation(SwingAnimationType type, int duration)
            {
                super(createInstance(SwingAnimation.class, type.getHandle(), duration));
            }
        }

        @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "net.minecraft.world.item.SwingAnimationType")
        public enum SwingAnimationType implements EnumWrapper
        {
            @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "NONE")
            NONE,

            @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "WHACK")
            WHACK,

            @Mapping(range = @Mapping.Range(from = Versions.V26_3, to = Versions.V26_3), path = "STAB")
            STAB;

            @Override
            public @NotNull Object getHandle()
            {
                return cast(this);
            }
        }
    }
}
