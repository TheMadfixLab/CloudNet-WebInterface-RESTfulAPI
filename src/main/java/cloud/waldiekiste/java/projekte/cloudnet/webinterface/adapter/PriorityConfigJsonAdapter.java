/*
 * Copyright (c) 2018.
 * Creative Commons Lizenzvertrag
 * CloudNet-Service-WebSocket-Extension von Phillipp Glanz ist lizenziert unter einer Creative Commons
 *  Namensnennung - Nicht kommerziell - Keine Bearbeitungen 4.0 International Lizenz.
 */

package cloud.waldiekiste.java.projekte.cloudnet.webinterface.adapter;

import com.google.gson.*;
import de.dytanic.cloudnet.lib.server.priority.PriorityConfig;

import java.lang.reflect.Type;

public class PriorityConfigJsonAdapter implements JsonSerializer<PriorityConfig>,JsonDeserializer<PriorityConfig> {
    @Override
    public PriorityConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        final int onlineServers = object.get("onlineServers").getAsInt();
        final int onlineCount = object.get("onlineCount").getAsInt();
        return new PriorityConfig(onlineServers,onlineCount);
    }

    @Override
    public JsonElement serialize(PriorityConfig priorityConfig, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.addProperty("onlineServers",priorityConfig.getOnlineServers());
        object.addProperty("onlineCount",priorityConfig.getOnlineCount());
        return object;
    }
}
