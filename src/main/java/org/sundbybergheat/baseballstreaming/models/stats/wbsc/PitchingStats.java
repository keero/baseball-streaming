package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PitchingStatsImpl.class)
@JsonDeserialize(as = PitchingStatsImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface PitchingStats {
  int year();

  String teamcode();

  String pos();

  @JsonProperty("pitch_win")
  int wins();

  @JsonProperty("pitch_loss")
  int losses();

  String era();

  @JsonProperty("pitch_appear")
  int appearances();

  @JsonProperty("pitch_gs")
  int gamesStarted();

  @JsonProperty("pitch_save")
  int saves();

  @JsonProperty("pitch_cg")
  int completeGames();

  @JsonProperty("pitch_sho")
  int shutouts();

  @JsonProperty("pitch_ip")
  String inningsPitched();

  @JsonProperty("pitch_h")
  int hitsAllowed();

  @JsonProperty("pitch_r")
  int runsAllowed();

  @JsonProperty("pitch_er")
  int earnedRunsAllowed();

  @JsonProperty("pitch_bb")
  int walksAllowed();

  @JsonProperty("pitch_so")
  int strikeouts();

  @JsonProperty("pitch_double")
  int doublesAllowed();

  @JsonProperty("pitch_triple")
  int triplesAllowed();

  @JsonProperty("pitch_hr")
  int homerunsAllowed();

  @JsonProperty("pitch_ab")
  int atBats();

  @JsonProperty("bavg")
  String opponentBattingAverage();

  @JsonProperty("pitch_wp")
  int wildPitches();

  @JsonProperty("pitch_hbp")
  int hitByPitch();

  @JsonProperty("pitch_bk")
  int balks();

  @JsonProperty("pitch_sfa")
  int sacrificeFliesAllowed();

  @JsonProperty("pitch_sha")
  int sacrificeHitsAllowed();

  @JsonProperty("pitch_ground")
  int groundOuts();

  @JsonProperty("pitch_fly")
  int flyOuts();

  @JsonProperty("pitch_whip")
  float walksAndHitsPerInningPitched();
}
