package ru.sovaowltv.model.api.twitch;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;
import ru.sovaowltv.service.data.DataExtractor;

@Getter
@ToString
public class TwitchWebHook {
    private final String channelId;
    private final String id;
    private final String time;
    private final String gameId;
    private final String streamTitle;

    public TwitchWebHook(JsonObject jObj, DataExtractor dataExtractor) {
        channelId = dataExtractor.getPrimitiveAsStringFromJson(jObj, "user_id");
        id = dataExtractor.getPrimitiveAsStringFromJson(jObj, "id");
        time = dataExtractor.getPrimitiveAsStringFromJson(jObj, "started_at");
        gameId = dataExtractor.getPrimitiveAsStringFromJson(jObj, "game_id");
        streamTitle = dataExtractor.getPrimitiveAsStringFromJson(jObj, "title");
    }
}
