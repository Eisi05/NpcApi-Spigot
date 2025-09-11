package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.Optional;

@Mapping(range = @Mapping.Range(from = Versions.V1_17, to = Versions.V1_21_6), path = "net.minecraft.world.entity.decoration.EntityArmorStand")
public class WrappedArmorStand extends WrappedEntity.WrappedNameTag<ArmorStand>
{
    private WrappedArmorStand(Object handle)
    {
        super(handle);
    }

    public static WrappedArmorStand create(World world)
    {
        return new WrappedArmorStand(
                createInstance(WrappedArmorStand.class, EntityTypes.ARMOR_STAND, Reflections.invokeMethod(world, "getHandle").get()));
    }

    public WrappedEntityData applyData(WrappedComponent component)
    {
        WrappedEntityData data = getEntityData();
        data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(0), (byte) 0x20);
        data.set(WrappedEntityData.EntityDataSerializers.OPTIONAL_CHAT_COMPONENT.create(2), Optional.of(component.getHandle()));
        data.set(WrappedEntityData.EntityDataSerializers.BOOLEAN.create(4), true);
        data.set(WrappedEntityData.EntityDataSerializers.BYTE.create(15), (byte) 0x10);

        return data;
    }
}
