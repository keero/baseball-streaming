package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Play(
    @JsonProperty("debug_lastplay") Optional<String> debugLastPlay,
    @JsonProperty("lastplayloaded") Optional<String> lastPlayLoaded,
    @JsonProperty("gameid") Optional<String> gameId,
    @JsonProperty("debugcode") Optional<String> debugCode,
    @JsonProperty("eventlocation") Optional<String> eventLocation,
    @JsonProperty("eventhome") Optional<String> eventHome,
    @JsonProperty("eventaway") Optional<String> eventAway,
    @JsonProperty("eventhomeid") Optional<String> eventHomeId,
    @JsonProperty("eventawayid") Optional<String> eventAwayId,
    @JsonProperty("eventurl") Optional<String> eventUrl,
    @JsonProperty("gameover") Optional<String> gameOver,
    @JsonProperty("innings") Optional<String> innings,
    @JsonProperty("platecount") @JsonSetter(nulls = Nulls.AS_EMPTY) List<PlateCount> plateCount,
    @JsonProperty("situation") Optional<Situation> situation,
    @JsonProperty("boxscore") @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, Player> boxScore,
    @JsonProperty("linescore") Optional<LineScore> lineScore,
    @JsonProperty("animation") Optional<Animation> animation,
    @JsonProperty("playdata") @JsonSetter(nulls = Nulls.AS_EMPTY) List<PlayData> playData) {}
