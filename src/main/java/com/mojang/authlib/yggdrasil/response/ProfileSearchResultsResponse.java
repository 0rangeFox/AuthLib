//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.authlib.yggdrasil.response;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

@Getter
@NoArgsConstructor
public class ProfileSearchResultsResponse extends Response {
    private GameProfile[] profiles;

    public static class Serializer implements JsonDeserializer<ProfileSearchResultsResponse> {

        public Serializer() {

        }

        public ProfileSearchResultsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ProfileSearchResultsResponse result = new ProfileSearchResultsResponse();

            if (json instanceof JsonObject) {
                JsonObject object = (JsonObject) json;

                if (object.has("error")) {
                    result.setError(object.getAsJsonPrimitive("error").getAsString());
                }

                if (object.has("errorMessage")) {
                    result.setError(object.getAsJsonPrimitive("errorMessage").getAsString());
                }

                if (object.has("cause")) {
                    result.setError(object.getAsJsonPrimitive("cause").getAsString());
                }
            } else {
                result.profiles = context.deserialize(json, GameProfile[].class);
            }

            return result;
        }

    }

}
