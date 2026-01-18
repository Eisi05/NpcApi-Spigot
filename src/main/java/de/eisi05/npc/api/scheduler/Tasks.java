package de.eisi05.npc.api.scheduler;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.objects.NpcSkin;
import de.eisi05.npc.api.utils.Reflections;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The {@link Tasks} class manages and starts various recurring tasks related to Non-Player Characters (NPCs) within the Bukkit environment. These tasks often
 * involve NPC behavior such as looking at nearby players.
 */
public class Tasks
{
    public static final Map<UUID, String> placeholderCache = new HashMap<>();
    private static BukkitTask lookAtTask;
    private static BukkitTask placeholderTask;

    /**
     * Starts all defined NPC-related tasks. This method should be called when the plugin is enabled to ensure that NPC behaviors are active.
     */
    public static void start()
    {
        lookAtTask();
        placeholderTask();
    }

    /**
     * Stops all defined NPC-related tasks.
     */
    public static void stop()
    {
        if(lookAtTask != null && !lookAtTask.isCancelled())
            lookAtTask.cancel();

        if(placeholderTask != null && !placeholderTask.isCancelled())
            placeholderTask.cancel();
    }

    /**
     * Implements a recurring task that makes NPCs look at nearby players. The task runs on a timer defined by {@code NpcApi.config.lookAtTimer()}. NPCs will
     * only look at players within a specified range, which is configured via {@link NpcOption#LOOK_AT_PLAYER}.
     */
    private static void lookAtTask()
    {
        lookAtTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for(NPC npc : NpcManager.getList())
                {
                    double range = npc.getOption(NpcOption.LOOK_AT_PLAYER);

                    if(range <= 0)
                        continue;

                    npc.entity.getBukkitPlayer().getNearbyEntities(range, range, range)
                            .stream().filter(entity -> entity instanceof Player)
                            .forEach(entity -> npc.lookAtPlayer((Player) entity));
                }
            }
        }.runTaskTimer(NpcApi.plugin, 0, NpcApi.config.lookAtTimer());
    }

    /**
     * Implements a recurring task that updates placeholders. The task runs on a timer defined by {@code NpcApi.config.placeholderTimer()}.
     */
    private static void placeholderTask()
    {
        placeholderTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Collection<NPC> npcs = NpcManager.getList();
                for(NPC npc : npcs)
                {
                    if(!npc.getNpcName().isStatic())
                        npc.updateNameForAll();
                }

                if(!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                    return;

                for(NPC npc : NpcManager.getList())
                {
                    NpcSkin npcSkin = npc.getOption(NpcOption.SKIN);
                    if(npcSkin == null || npcSkin.isStatic() || npcSkin.getSkin() == null || !npc.entity.equals(npc.getServerPlayer()))
                        continue;

                    npc.updateSkin(player ->
                    {
                        String newPlaceholder = (String) Reflections.invokeStaticMethod("me.clip.placeholderapi.PlaceholderAPI",
                                "setPlaceholders", player, npcSkin.getSkin().name()).get();

                        String old = placeholderCache.getOrDefault(player.getUniqueId(), null);

                        if(old != null && !newPlaceholder.equals(old))
                        {
                            placeholderCache.put(player.getUniqueId(), newPlaceholder);
                            return true;
                        }
                        return false;
                    });
                }
            }
        }.runTaskTimer(NpcApi.plugin, NpcApi.config.placeholderTimer(), NpcApi.config.placeholderTimer());
    }
}
