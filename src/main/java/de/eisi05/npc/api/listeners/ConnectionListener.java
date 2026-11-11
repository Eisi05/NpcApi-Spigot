package de.eisi05.npc.api.listeners;

import de.eisi05.npc.api.NpcApi;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcOption;
import de.eisi05.npc.api.objects.NpcSkin;
import de.eisi05.npc.api.scheduler.Tasks;
import de.eisi05.npc.api.utils.PacketReader;
import de.eisi05.npc.api.wrapper.objects.WrappedPlayerTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ConnectionListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event)
    {
        if(!BedrockConnectionListener.isBedrockPlayer(event.getPlayer()))
            join(event.getPlayer(), 10L);
    }

    static void join(Player player, long delay)
    {
        PacketReader.inject(player);

        WrappedPlayerTeam.clear(player.getUniqueId());

        if(!NpcApi.config.autoManageVisibility())
            return;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if(player == null || !player.isOnline())
                    return;

                for(NPC npc : NpcManager.getList())
                {
                    if(!npc.getVisibilityManager().shouldShowToPlayer(player.getUniqueId()))
                        continue;

                    npc.showNPCToPlayer(player);
                    npc.addWalkingViewer(player);
                    NpcSkin npcSkin = npc.getOption(NpcOption.SKIN, player);
                    if(npcSkin == null || npcSkin.isStatic() || npcSkin.getPlaceholder() == null || npc.getOption(NpcOption.USE_PLAYER_SKIN, player))
                        continue;

                    Tasks.updateSkin(player, npc, npcSkin);
                }
            }
        }.runTaskLater(NpcApi.plugin, delay);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        PacketReader.uninject(event.getPlayer());
        Tasks.placeholderCache.remove(event.getPlayer().getUniqueId());
        NpcDamageListener.cleanUpAttack(event.getPlayer());

        for(NPC npc : NpcManager.getList())
        {
            npc.nameCache.remove(event.getPlayer().getUniqueId());
            npc.removeWalkingViewer(event.getPlayer());

            if(NpcApi.config.autoManageVisibility())
                npc.hideNpcFromPlayer(event.getPlayer());
        }
    }
}
