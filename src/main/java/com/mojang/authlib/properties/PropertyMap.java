package com.mojang.authlib.properties;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

public class PropertyMap
        extends ForwardingMultimap<String, Property> {
    private final Multimap<String, Property> properties = LinkedHashMultimap.create();

    protected Multimap<String, Property> delegate() {
        return this.properties;
    }

    public static class Serializer
implements JsonSerializer<PropertyMap>,
            JsonDeserializer<PropertyMap> {
        public PropertyMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            PropertyMap result;
            block5: {
                block4: {
                    result = new PropertyMap();
                    if (!(json instanceof JsonObject)) break block4;
                    JsonObject object = (JsonObject)((Object)json);
                    for (Map.Entry entry : object.entrySet()) {
                        if (!(entry.getValue() instanceof JsonArray)) continue;
                        Iterator i$ = ((JsonArray)((Object)entry.getValue())).iterator();
                        while (i$.hasNext()) {
                            JsonElement element = (JsonElement)((Object)i$.next());
                            result.put((String)entry.getKey(), new Property((String)entry.getKey(), element.getAsString()));
                        }
                    }
                    break block5;
                }
                if (!(json instanceof JsonArray)) break block5;
                Iterator i$ = ((JsonArray)((Object)json)).iterator();
                while (i$.hasNext()) {
                    JsonElement element = (JsonElement)((Object)i$.next());
                    if (!(element instanceof JsonObject)) continue;
                    JsonObject object = (JsonObject)((Object)element);
                    String name = object.getAsJsonPrimitive("name").getAsString();
                    String value = object.getAsJsonPrimitive("value").getAsString();
                    if (object.has("signature")) {
                        result.put(name, new Property(name, value, object.getAsJsonPrimitive("signature").getAsString()));
                        continue;
                    }
                    result.put(name, new Property(name, value));
                }
            }
            return result;
        }

        public JsonElement serialize(PropertyMap src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray result = new JsonArray();
            for (Property property : src.values()) {
                JsonObject object = new JsonObject();
                object.addProperty("name", property.getName());
                object.addProperty("value", property.getValue());
                if (property.hasSignature()) {
                    object.addProperty("signature", property.getSignature());
                }
                result.add((JsonElement)((Object)object));
            }
            return result;
        }
    }


}
