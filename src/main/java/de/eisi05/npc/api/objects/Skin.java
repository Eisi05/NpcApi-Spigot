package de.eisi05.npc.api.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.eisi05.npc.api.utils.Reflections;
import de.eisi05.npc.api.utils.Versions;
import de.eisi05.npc.api.utils.exceptions.VersionNotFound;
import de.eisi05.npc.api.wrapper.objects.WrappedServerPlayer;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
     * Fetches a {@link Skin} from Mojang by UUID.
     *
     * @param uuid the UUID of the player.
     * @return an {@link Optional} containing the skin if found, otherwise empty.
     */
    public static @Nullable Optional<Skin> fetchSkin(@NotNull UUID uuid)
    {
        if(skinCache.containsKey(uuid))
            return Optional.of(skinCache.get(uuid));

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
                    return Optional.of(skin);
                }

                return Optional.empty();
            }
        } catch(IOException e)
        {
            return Optional.empty();
        }
    }

    /**
     * Fetches a {@link Skin} by player name.
     *
     * @param name the player username.
     * @return an {@link Optional} containing the skin if found, otherwise empty.
     */
    public static Optional<Skin> fetchSkin(@NotNull String name)
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
        } catch(IOException e)
        {
            return Optional.empty();
        }
    }

    /**
     * Uploads a skin file to MineSkin and retrieves the resulting {@link Skin}.
     *
     * @param skinFile the PNG file of the skin to upload.
     * @return an {@link Optional} containing the skin if successful, otherwise empty.
     * @throws IllegalArgumentException if the file does not exist.
     */
    public static Optional<Skin> fetchSkin(@NotNull File skinFile)
    {
        if(!skinFile.exists())
            throw new IllegalArgumentException("File does not exist");

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10_000)
                .setSocketTimeout(10_000)
                .build();

        String boundary = "----MineSkinBoundary" + System.currentTimeMillis();

        try(CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
                FileInputStream fis = new FileInputStream(skinFile))
        {
            HttpPost upload = new HttpPost("https://api.mineskin.org/generate/upload");
            upload.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(skinFile.getName()).append("\"\r\n");
            writer.append("Content-Type: image/png\r\n\r\n");
            writer.flush();

            byte[] buffer = new byte[4096];
            int len;
            while((len = fis.read(buffer)) != -1)
                baos.write(buffer, 0, len);
            baos.flush();

            writer.append("\r\n--").append(boundary).append("--\r\n");
            writer.flush();

            upload.setEntity(new ByteArrayEntity(baos.toByteArray()));

            return httpClient.execute(upload, response ->
            {
                HttpEntity responseEntity = response.getEntity();
                String json = EntityUtils.toString(responseEntity);

                JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
                JsonObject texture = obj.getAsJsonObject("data").getAsJsonObject("texture");

                String value = texture.get("value").getAsString();
                String signature = texture.get("signature").getAsString();

                return Optional.of(new Skin(null, value, signature));
            });
        } catch(IOException e)
        {
            return Optional.empty();
        }
    }

    /**
     * Asynchronously fetches a skin by the player's UUID.
     *
     * @param uuid the UUID of the player
     * @return a CompletableFuture containing an Optional of the Skin
     */
    public static CompletableFuture<Optional<Skin>> fetchSkinAsync(@NotNull UUID uuid)
    {
        return CompletableFuture.supplyAsync(() -> fetchSkin(uuid));
    }

    /**
     * Asynchronously fetches a skin by the player's name.
     *
     * @param name the name of the player
     * @return a CompletableFuture containing an Optional of the Skin
     */
    public static CompletableFuture<Optional<Skin>> fetchSkinAsync(@NotNull String name)
    {
        return CompletableFuture.supplyAsync(() -> fetchSkin(name));
    }

    /**
     * Asynchronously fetches a skin from a local file.
     *
     * @param skinFile the PNG file containing the skin
     * @return a CompletableFuture containing an Optional of the Skin
     */
    public static CompletableFuture<Optional<Skin>> fetchSkinAsync(@NotNull File skinFile)
    {
        return CompletableFuture.supplyAsync(() -> fetchSkin(skinFile));
    }
}
