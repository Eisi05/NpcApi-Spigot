package de.eisi05.npc.api.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.utils.exceptions.VersionNotFound;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a player's skin, containing its name, value, and signature.
 * This record is immutable and implements {@link Serializable} for easy persistence.
 * The skin value and signature are typically obtained from Mojang's session servers
 * and are used to display the correct player texture.
 *
 * @param name      The name associated with the skin (usually the player's username). Can be {@code null}.
 * @param value     The base64 encoded string representing the skin data (texture URL, model, etc.).
 * @param signature The signature used to verify the authenticity of the skin data.
 */
public record Skin(@Nullable String name, @NotNull String value, @NotNull String signature) implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * A static cache to store fetched skins, mapping UUIDs to Skin objects.
     * This helps reduce redundant API calls to Mojang's servers.
     */
    private static final Map<UUID, Skin> skinCache = new HashMap<>();

    /**
     * Retrieves the skin data directly from a currently online Bukkit player.
     * This method uses reflection to access the player's game profile properties.
     *
     * @param player The Bukkit player from whom to retrieve the skin. Must not be {@code null}.
     * @return A {@link Skin} object representing the player's current skin, or {@code null} if no skin properties are found.
     * @throws VersionNotFound If the API does not support the current server version.
     */
    public static @Nullable Skin fromPlayer(@NotNull Player player)
    {
        var properties = WrappedServerPlayer.fromPlayer(player).getGameProfile().getProperties().get("textures").iterator();

        if(!properties.hasNext())
            return null;

        var property = properties.next();

        if(Versions.getVersion() == Versions.NONE)
            throw new VersionNotFound();

        return Versions.isCurrentVersionSmallerThan(Versions.V1_20_2) ?
                new Skin(player.getName(), Reflections.<String>invokeMethod(property, "getValue").get(),
                        Reflections.<String>invokeMethod(property, "getSignature").get()) :
                new Skin(player.getName(), Reflections.<String>invokeMethod(property, "value").get(),
                        Reflections.<String>invokeMethod(property, "signature").get());
    }

    /**
     * Fetches a player's skin from Mojang's session server using their UUID.
     * This method first checks the local cache (`skinCache`) before making an HTTP request.
     * The fetched skin is added to the cache for future use.
     *
     * @param uuid The UUID of the player whose skin is to be fetched. Must not be {@code null}.
     * @return A {@link Skin} object if the skin is successfully fetched or found in cache, otherwise {@code null}.
     * @throws VersionNotFound If the API does not support the current server version (though this check might be redundant here).
     */
    private static @Nullable Skin fetchSkin(@NotNull UUID uuid)
    {

        if(skinCache.containsKey(uuid))
            return skinCache.get(uuid);

        try
        {
            URL url = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);

            try(InputStream is = connection.getInputStream(); Scanner scanner = new Scanner(is))
            {
                String response = scanner.useDelimiter("\\A").next();

                JsonObject json = new JsonParser().parse(response).getAsJsonObject();
                String name = json.get("name").getAsString();

                JsonArray properties = json.getAsJsonArray("properties");
                for(JsonElement prop : properties)
                {
                    JsonObject obj = prop.getAsJsonObject();
                    String value = obj.get("value").getAsString();
                    String signature = obj.has("signature") ? obj.get("signature").getAsString() : null;
                    if(Versions.getVersion() == Versions.NONE)
                        throw new VersionNotFound();

                    Skin skin = new Skin(name, value, signature);
                    skinCache.put(uuid, skin);
                    return skin;
                }

                return null;
            }
        } catch(Exception e)
        {
            return null;
        }
    }

    /**
     * Fetches a player's skin by their username.
     * This method first calls Mojang's API to get the player's UUID from their name
     * and then uses the UUID to fetch the skin data.
     *
     * @param name The username of the player whose skin is to be fetched. Must not be {@code null}.
     * @return A {@link Skin} object if the skin is successfully fetched, otherwise {@code null}.
     */
    private static Skin fetchSkin(@NotNull String name)
    {
        try
        {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);

            try(InputStream is = conn.getInputStream(); Scanner scanner = new Scanner(is))
            {
                String response = scanner.useDelimiter("\\A").next();
                JsonObject json = new JsonParser().parse(response).getAsJsonObject();
                String id = json.get("id").getAsString();
                return fetchSkin(UUID.fromString(id.replaceFirst(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5")));
            }
        } catch(Exception e)
        {
            return null;
        }
    }

    /**
     * Asynchronously fetches a player's skin using their UUID.
     * This method wraps the synchronous {@link #fetchSkin(UUID)} call in a {@link CompletableFuture}
     * to prevent blocking the main thread.
     *
     * @param uuid The UUID of the player whose skin is to be fetched. Must not be {@code null}.
     * @return A {@link CompletableFuture} that will complete with the {@link Skin} object, or {@code null} if fetching fails.
     */
    public static CompletableFuture<Skin> fetchSkinAsync(@NotNull UUID uuid)
    {
        return CompletableFuture.supplyAsync(() -> fetchSkin(uuid));
    }

    /**
     * Asynchronously fetches a player's skin using their username.
     * This method wraps the synchronous {@link #fetchSkin(String)} call in a {@link CompletableFuture}
     * to prevent blocking the main thread.
     *
     * @param name The username of the player whose skin is to be fetched. Must not be {@code null}.
     * @return A {@link CompletableFuture} that will complete with the {@link Skin} object, or {@code null} if fetching fails.
     */
    public static CompletableFuture<Skin> fetchSkinAsync(@NotNull String name)
    {
        return CompletableFuture.supplyAsync(() -> fetchSkin(name));
    }
}
