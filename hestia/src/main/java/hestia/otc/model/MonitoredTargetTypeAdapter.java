package hestia.otc.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializes and deserializes {@link MonitoredTarget} instances using the
 * {@code targetType} JSON property.
 */
public class MonitoredTargetTypeAdapter implements JsonSerializer<MonitoredTarget>, JsonDeserializer<MonitoredTarget> {
    private static final String TYPE_PROPERTY = "targetType";

    @Override
    public JsonElement serialize(MonitoredTarget source, Type typeOfSource, JsonSerializationContext context) {
        JsonObject jsonObject;
        if (source instanceof Database) {
            jsonObject = context.serialize(source, Database.class).getAsJsonObject();
            jsonObject.addProperty(TYPE_PROPERTY, "database");
        } else if (source instanceof Server) {
            jsonObject = context.serialize(source, Server.class).getAsJsonObject();
            jsonObject.addProperty(TYPE_PROPERTY, "server");
        } else if (source instanceof Site) {
            jsonObject = context.serialize(source, Site.class).getAsJsonObject();
            jsonObject.addProperty(TYPE_PROPERTY, "site");
        } else {
            throw new JsonParseException("Unsupported MonitoredTarget implementation: " + source.getClass().getName());
        }
        return jsonObject;
    }

    @Override
    public MonitoredTarget deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement typeElement = jsonObject.get(TYPE_PROPERTY);
        if (typeElement == null || typeElement.isJsonNull()) {
            throw new JsonParseException("Missing JSON property: " + TYPE_PROPERTY);
        }
        String targetType = typeElement.getAsString();
        return switch (targetType) {
        case "database" -> context.deserialize(jsonObject, Database.class);
        case "server" -> context.deserialize(jsonObject, Server.class);
        case "site" -> context.deserialize(jsonObject, Site.class);
        default -> throw new JsonParseException("Unsupported targetType: " + targetType);
        };
    }
}
