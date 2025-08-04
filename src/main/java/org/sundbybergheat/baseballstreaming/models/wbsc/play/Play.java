package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record Play(
    @JsonProperty("debug_lastplay") String debugLastPlay,
    @JsonProperty("lastplayloaded") String lastPlayLoaded,
    @JsonProperty("gameid") String gameId,
    @JsonProperty("debugcode") String debugCode,
    @JsonProperty("eventlocation") String eventLocation,
    @JsonProperty("eventhome") String eventHome,
    @JsonProperty("eventaway") String eventAway,
    @JsonProperty("eventhomeid") String eventHomeId,
    @JsonProperty("eventawayid") String eventAwayId,
    @JsonProperty("eventurl") String eventUrl,
    @JsonProperty("gameover") String gameOver,
    @JsonProperty("innings") String innings,
    @JsonProperty("platecount") List<PlateCount> plateCount,
    @JsonProperty("situation") Situation situation,
    @JsonProperty("boxscore") Map<String, Player> boxScore,
    @JsonProperty("linescore") LineScore lineScore,
    @JsonProperty("animation") Animation animation,
    @JsonProperty("playdata") List<PlayData> playData) {}
