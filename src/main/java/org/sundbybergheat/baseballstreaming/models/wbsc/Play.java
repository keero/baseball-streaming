package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PlayImpl.class)
@JsonDeserialize(as = PlayImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Play {

  @JsonIgnore
  @Default
  default int playNumber() {
    return 0;
  }

  @JsonProperty("debug_lastplay")
  int debugLastPlay();

  @JsonProperty("lastplayloaded")
  int lastPlayLoaded();

  @JsonProperty("gameid")
  String gameId();

  @JsonProperty("debugcode")
  String debugCode();

  @JsonProperty("eventlocation")
  String eventLocation();

  @JsonProperty("eventhome")
  String eventHome();

  @JsonProperty("eventaway")
  String eventAway();

  @JsonProperty("eventhomeid")
  String eventHomeId();

  @JsonProperty("eventawayid")
  String eventAwayId();

  @JsonProperty("eventurl")
  String eventUrl();

  @JsonProperty("gameover")
  int gameOver();

  String innings();

  @JsonProperty("platecount")
  @Default
  default List<PlateCount> plateCount() {
    return Collections.emptyList();
  }

  Situation situation();

  @JsonProperty("boxscore")
  @Default
  default Map<String, BoxScore> boxScore() {
    return Collections.emptyMap();
  }

  @JsonProperty("linescore")
  LineScore lineScore();

  Animation animation();

  @JsonProperty("playdata")
  @Default
  default List<PlayData> playData() {
    return Collections.emptyList();
  }
}
