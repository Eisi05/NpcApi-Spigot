package de.eisi05.npc.api.wrapper.packets;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "net.minecraft.network.protocol.game.ServerboundPlayerActionPacket")
@Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayInBlockDig")
public class PlayerActionPacket extends PacketWrapper.PacketHolder
{
    protected PlayerActionPacket(Object handle)
    {
        super(handle);
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "getAction")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "g")
    public @Nullable Action getAction()
    {
        Object invoke = invokeWrappedMethod();
        for(Action action : Action.values())
        {
            if(action.getHandle() == invoke)
                return action;
        }
        return null;
    }

    @Mapping(range = @Mapping.Range(from = Versions.V26_1, to = Versions.V26_3), path = "net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action")
    @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "net.minecraft.network.protocol.game.PacketPlayInBlockDig$EnumPlayerDigType")
    public enum Action implements EnumWrapper
    {
        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "START_DESTROY_BLOCK")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "a")
        START_DESTROY_BLOCK,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "ABORT_DESTROY_BLOCK")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "b")
        ABORT_DESTROY_BLOCK,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "STOP_DESTROY_BLOCK")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "c")
        STOP_DESTROY_BLOCK,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "DROP_ALL_ITEMS")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "d")
        DROP_ALL_ITEMS,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "DROP_ITEM")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "e")
        DROP_ITEM,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "RELEASE_USE_ITEM")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "f")
        RELEASE_USE_ITEM,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "SWAP_ITEM_WITH_OFFHAND")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "g")
        SWAP_ITEM_WITH_OFFHAND,

        @Mapping(range = @Mapping.Range(from = Versions.V1_21_11, to = Versions.V26_3), path = "STAB")
        @Mapping(fixed = @Mapping.Fixed(Versions.V1_21_11), path = "h")
        STAB;

        @Override
        public @NotNull Object getHandle()
        {
            return cast(this);
        }
    }
}
