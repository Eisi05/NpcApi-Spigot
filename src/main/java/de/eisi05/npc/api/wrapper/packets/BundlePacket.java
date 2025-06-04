package de.eisi05.npc.api.wrapper.packets;

import org.jetbrains.annotations.NotNull;

public class BundlePacket extends PacketWrapper
{
    private final PacketWrapper[] packets;

    public BundlePacket(@NotNull PacketWrapper... packets)
    {
        super(null);
        this.packets = packets;
    }

    public @NotNull PacketWrapper[] getPackets()
    {
        return packets;
    }
}
