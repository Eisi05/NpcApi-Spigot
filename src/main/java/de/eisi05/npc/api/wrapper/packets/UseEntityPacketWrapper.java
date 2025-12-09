package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import de.eisi05.npc.api.wrapper.enums.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayInUseEntity")
public class UseEntityPacketWrapper extends PacketWrapper.PacketHolder
{
    @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_11), path = "e")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_20_6), path = "d")
    public final static Action ATTACK_ACTION = new Action(getStaticWrappedFieldValue("ATTACK_ACTION").orElse(null));

    protected UseEntityPacketWrapper(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_11), path = "b")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_20_6), path = "a")
    public int getId()
    {
        return getWrappedFieldValue();
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_11), path = "c")
    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_20_6), path = "b")
    public @NotNull Action getAction()
    {
        Object value = getWrappedFieldValue();

        if(value.equals(ATTACK_ACTION.getHandle()))
            return ATTACK_ACTION;

        return new Action(value);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game" +
            ".PacketPlayInUseEntity$b")
    public enum ActionType implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        INTERACT,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "b")
        ATTACK,

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "c")
        INTERACT_AT;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }

    @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "net.minecraft.network.protocol.game" +
            ".PacketPlayInUseEntity$EnumEntityUseAction")
    public static class Action extends Wrapper
    {
        public Action(Object handle)
        {
            super(handle);
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        public @Nullable ActionType getActionType()
        {
            Object enumHandle = invokeWrappedMethod();
            for(ActionType type : ActionType.values())
            {
                if(type.getHandle().equals(enumHandle))
                    return type;
            }
            return null;
        }

        @Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_11), path = "a")
        public @Nullable InteractionHand getHand()
        {
            try
            {
                Object enumHandle = getWrappedFieldValue();
                for(InteractionHand type : InteractionHand.values())
                {
                    if(type.getHandle().equals(enumHandle))
                        return type;
                }
                return null;
            } catch(RuntimeException e)
            {
                if(e.getCause() instanceof RuntimeException ex && ex.getCause() instanceof NoSuchFieldException)
                    return null;
                else
                    throw e;
            }
        }
    }
}
