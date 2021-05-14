package com.mojang.authlib.yggdrasil;

import com.google.gson.*;
import com.mojang.authlib.*;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.UUIDTypeAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;

public class YggdrasilAuthenticationService extends HttpAuthenticationService {
    @Getter private final String clientToken;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(GameProfile.class, new GameProfileSerializer())
            .registerTypeAdapter(PropertyMap.class, new Serializer())
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .registerTypeAdapter(ProfileSearchResultsResponse.class, new ProfileSearchResultsResponse.Serializer())
            .create();

    public YggdrasilAuthenticationService(Proxy proxy, String clientToken) {
        super(proxy);
        this.clientToken = clientToken;
    }

    public UserAuthentication createUserAuthentication(Agent agent) {
        return new YggdrasilUserAuthentication(this, agent);
    }

    public MinecraftSessionService createMinecraftSessionService() {
        return new YggdrasilMinecraftSessionService(this);
    }

    public GameProfileRepository createProfileRepository() {
        return new YggdrasilGameProfileRepository(this);
    }

    protected <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
        try {
            String jsonResult = input == null ? this.performGetRequest(url) : this.performPostRequest(url, this.gson.toJson(input), "application/json");
            T result = this.gson.fromJson(jsonResult, classOfT);
            if (result == null) {
                return null;
            }

            if (StringUtils.isNotBlank(result.getError())) {
                if ("UserMigratedException".equals(result.getCause())) {
                    throw new UserMigratedException(result.getErrorMessage());
                }

                if (result.getError().equals("ForbiddenOperationException")) {
                    throw new InvalidCredentialsException(result.getErrorMessage());
                }

                throw new AuthenticationException(result.getErrorMessage());
            }

            return result;
        } catch (IOException | IllegalStateException | JsonParseException e) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", e);
        }
    }

    @NoArgsConstructor
    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {

        public GameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = (JsonObject) json;

            UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
            String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;

            return new GameProfile(id, name);
        }

        public JsonElement serialize(GameProfile src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            if (src.getId() != null) {
                result.add("id", context.serialize(src.getId()));
            }

            if (src.getName() != null) {
                result.addProperty("name", src.getName());
            }

            return result;
        }

    }

}
