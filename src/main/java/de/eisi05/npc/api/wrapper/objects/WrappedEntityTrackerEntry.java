package de.eisi05.npc.api.wrapper.objects;

import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.wrapper.Mapping;
import de.eisi05.npc.api.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mapping(range = @Mapping.Range(from = Versions.V1_21, to = Versions.V1_21_5), path = "net.minecraft.server.level.EntityTrackerEntry")
public class WrappedEntityTrackerEntry extends Wrapper
{
    public WrappedEntityTrackerEntry(@NotNull WrappedEntity<?> entity)
    {
        super(createHandle(entity));
    }

    private static @NotNull Object createHandle(@NotNull WrappedEntity<?> entity)
    {
        if(Versions.isCurrentVersionSmallerThan(Versions.V1_21_5))
            return createInstance(WrappedEntityTrackerEntry.class, entity.getServer(), entity, 0, false, (Consumer<Object>) o ->
            {
            }, Set.of());
        else
            return createInstance(WrappedEntityTrackerEntry.class, entity.getServer(), entity, 0, false, (Consumer<Object>) o ->
            {
            }, (BiConsumer<Object, List<UUID>>) (o, uuids) ->
            {
            }, Set.of());
    }
}
