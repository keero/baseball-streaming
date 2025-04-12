package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.jetbrains.annotations.Nullable;

@Immutable
@JsonSerialize(as = BattingStatsImpl.class)
@JsonDeserialize(as = BattingStatsImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface BattingStats {
  int year();

  String teamcode();

  @JsonProperty("pos")
  String position();

  @JsonProperty("g")
  int games();

  @JsonProperty("gs")
  int gamesStarted();

  @JsonProperty("ab")
  int atBats();

  @JsonProperty("r")
  int runs();

  @JsonProperty("h")
  int hits();

  @Nullable
  @JsonProperty("double")
  Integer doubles();

  @Nullable
  @JsonProperty("triple")
  Integer triples();

  @Nullable
  @JsonProperty("hr")
  Integer homeruns();

  int rbi();

  @Nullable
  @JsonProperty("tb")
  Integer totalBases();

  @JsonProperty("avg")
  String battingAverage();

  @Nullable
  @JsonProperty("slg")
  String slugging();

  @Nullable
  @JsonProperty("obp")
  String onBasePercentage();

  @Nullable
  @JsonProperty("ops")
  String onBasePercentagePlusSlugging();

  @Nullable
  @JsonProperty("bb")
  Integer walks();

  @Nullable
  @JsonProperty("hbp")
  Integer hitByPitch();

  @Nullable
  @JsonProperty("so")
  Integer strikeouts();

  @Nullable
  @JsonProperty("gdp")
  Integer groundoutDoublePlay();

  @Nullable
  @JsonProperty("sf")
  Integer sacrificeFlies();

  @Nullable
  @JsonProperty("sh")
  Integer sacrificeHits();

  @Nullable
  @JsonProperty("sb")
  Integer stolenBases();

  @Nullable
  @JsonProperty("cs")
  Integer caughtStealing();
}
