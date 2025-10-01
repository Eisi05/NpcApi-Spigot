package de.eisi05.npc.api;

import de.eisi05.npc.api.listeners.ChangeWorldListener;
import de.eisi05.npc.api.listeners.ConnectionListener;
import de.eisi05.npc.api.listeners.NpcInteractListener;
import de.eisi05.npc.api.manager.NpcManager;
import de.eisi05.npc.api.objects.NPC;
import de.eisi05.npc.api.objects.NpcConfig;
import de.eisi05.npc.api.pathfinding.Path;
import de.eisi05.npc.api.scheduler.Tasks;
import de.eisi05.npc.api.utils.PacketReader;
import de.eisi05.npc.api.wrapper.objects.WrappedPlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * The main entry point and singleton class for the NPC API.
 * This class handles the initialization, configuration, and shutdown
 * of the NPC functionality within a Bukkit plugin.
 */
public final class NpcApi
{
    /**
     * A static reference to the Bukkit plugin instance that is using this API.
     * This is set during the API's initialization.
     */
    public static Plugin plugin;

    public static Function<Player, String> DISABLED_MESSAGE_PROVIDER = player -> ChatColor.RED + "DISABLED";

    /**
     * The configuration object for the NPC API, containing various settings
     * like the look-at timer.
     */
    public static NpcConfig config;

    private static NpcApi npcApi;

    /**
     * Private constructor to enforce the singleton pattern.
     * Initializes the API by registering listeners, loading existing NPCs,
     * injecting packet readers, and starting recurring tasks.
     *
     * @param plugin The {@link JavaPlugin} instance using this API. Must not be {@code null}.
     * @param config The {@link NpcConfig} object for the API. Must not be {@code null}.
     */
    private NpcApi(@NotNull JavaPlugin plugin, @NotNull NpcConfig config)
    {
        NpcApi.plugin = plugin;
        NpcApi.config = config;

        Bukkit.getPluginManager().registerEvents(new ChangeWorldListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new NpcInteractListener(), plugin);

        ConfigurationSerialization.registerClass(Path.class);

        NpcManager.loadNPCs();
        PacketReader.injectAll();

        Tasks.start();
    }

    /**
     * Creates or retrieves the singleton instance of the {@code NpcApi} with a default configuration.
     * If the API instance does not exist or the provided plugin is null, a new instance is created.
     *
     * @param plugin The {@link JavaPlugin} instance using this API. Must not be {@code null}.
     * @return The singleton {@link NpcApi} instance. Must not be {@code null}.
     */
    public static @NotNull NpcApi createInstance(@NotNull JavaPlugin plugin)
    {
        return createInstance(plugin, new NpcConfig());
    }

    /**
     * Creates or retrieves the singleton instance of the {@code NpcApi} with a custom configuration.
     * If the API instance does not exist or the provided plugin is null, a new instance is created.
     *
     * @param plugin The {@link JavaPlugin} instance using this API. Must not be {@code null}.
     * @param config The {@link NpcConfig} object to use for the API. Must not be {@code null}.
     * @return The singleton {@link NpcApi} instance. Must not be {@code null}.
     */
    public static @NotNull NpcApi createInstance(@NotNull JavaPlugin plugin, @NotNull NpcConfig config)
    {
        if(npcApi == null || plugin == null)
            npcApi = new NpcApi(plugin, config);

        return npcApi;
    }

    /**
     * Sets a function that provides the message shown when an NPC is disabled.
     *
     * @param function a {@link Function} that takes a {@link Player} and returns the disabled message
     * @return this {@link NpcApi} instance for method chaining
     */
    public @NotNull NpcApi setDisabledMessageProvider(Function<Player, String> function)
    {
        DISABLED_MESSAGE_PROVIDER = function;
        return this;
    }

    /**
     * Disables the NPC API, performing the necessary cleanup.
     * This includes hiding all active NPCs from players, clearing the NPC manager,
     * and uninjecting packet readers. It also nullifies the static references.
     */
    public static void disable()
    {
        NpcManager.getList().forEach(NPC::hideNpcFromAllPlayers);
        NpcManager.clear();
        PacketReader.uninjectAll();
        Tasks.stop();
        WrappedPlayerTeam.clear();
        ConfigurationSerialization.unregisterClass(Path.class);

        npcApi = null;
        plugin = null;
    }
}
