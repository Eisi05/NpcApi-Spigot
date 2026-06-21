package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import org.bukkit.World;
import org.bukkit.entity.Interaction;


@Mapping(range = @Mapping.Range(from = Versions.V1_20_6, to = Versions.V26_2), path = "net.minecraft.world.entity.Interaction")
public class WrappedInteraction extends WrappedEntity<Interaction>
{
    WrappedInteraction(Object handle)
    {
        super(handle);
    }

    public static WrappedInteraction create(World world)
    {
        return new WrappedInteraction(
                createInstance(WrappedInteraction.class, EntityTypes.INTERACTION, Reflections.invokeMethod(world, "getHandle").get()));
    }

    public WrappedEntityData getData(float width, float height)
    {
        WrappedEntityData data = getEntityData();
        data.set(WrappedEntityData.EntityDataSerializers.FLOAT.create(8), width);
        data.set(WrappedEntityData.EntityDataSerializers.FLOAT.create(9), height);
        return data;
    }
}
