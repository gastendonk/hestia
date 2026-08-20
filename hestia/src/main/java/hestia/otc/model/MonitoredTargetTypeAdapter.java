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
        for (Class<? extends MonitoredTarget> targetClass : MonitoredTarget.CLASSES) {
            if (targetClass.isInstance(source)) {
                JsonObject jsonObject = context.serialize(source, targetClass).getAsJsonObject();
                jsonObject.addProperty(TYPE_PROPERTY, targetClass.getSimpleName().toLowerCase());
                return jsonObject;
            }
        }
        throw new JsonParseException("Unsupported MonitoredTarget implementation: " + source.getClass().getName());
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
        for (Class<? extends MonitoredTarget> targetClass : MonitoredTarget.CLASSES) {
            if (targetClass.getSimpleName().toLowerCase().equals(targetType)) {
                return context.deserialize(jsonObject, targetClass);
            }
        }
        throw new JsonParseException("Unsupported targetType: " + targetType);
    }
}
