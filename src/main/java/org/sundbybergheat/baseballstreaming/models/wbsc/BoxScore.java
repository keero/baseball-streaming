package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = BoxScoreImpl.class)
@JsonDeserialize(as = BoxScoreImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface BoxScore {
  String name();

  @JsonProperty("firstname")
  String firstName();

  @JsonProperty("lastname")
  String lastName();

  @JsonProperty("playerid")
  String playerId();

  @JsonProperty("teamid")
  String teamId();

  @JsonProperty("teamcode")
  String teamCode();

  String image();

  @JsonProperty("inplay")
  String inPlay();

  int reentry();

  @JsonProperty("POS")
  Optional<String> position();

  @JsonProperty("AB")
  Optional<Integer> atBats();

  @JsonProperty("R")
  Optional<Integer> runs();

  @JsonProperty("H")
  Optional<Integer> hits();

  @JsonProperty("RBI")
  Optional<Integer> runsBattedIn();

  @JsonProperty("BB")
  Optional<Integer> walks();

  @JsonProperty("SO")
  Optional<Integer> strikeOuts();

  @JsonProperty("DOUBLE")
  Optional<Integer> doubles();

  @JsonProperty("TRIPLE")
  Optional<Integer> triples();

  @JsonProperty("HR")
  Optional<Integer> homeruns();

  @JsonProperty("SB")
  Optional<Integer> stolenBases();

  @JsonProperty("CS")
  Optional<Integer> caughtStealing();

  @JsonProperty("LOB")
  Optional<Integer> leftOnBase();

  @JsonProperty("AVG")
  Optional<String> average();

  @JsonProperty("OBP")
  Optional<String> onBasePercentage();

  @JsonProperty("SLG")
  Optional<String> slugging();

  @JsonProperty("OPS")
  Optional<String> obpPlusSlugging();

  @JsonProperty("PO")
  int putOuts();

  @JsonProperty("A")
  int assists();

  @JsonProperty("E")
  int errors();

  @JsonProperty("PITCHIP")
  Optional<String> inningsPitched();

  @JsonProperty("PITCHER")
  Optional<Integer> earnedRuns();

  @JsonProperty("PITCHR")
  Optional<Integer> pitcherRuns();

  @JsonProperty("PITCHH")
  Optional<Integer> pitcherHits();

  @JsonProperty("PITCHBB")
  Optional<Integer> pitcherWalks();

  @JsonProperty("PITCHSO")
  Optional<Integer> pitcherStrikeouts();

  @JsonProperty("PITCHES")
  Optional<Integer> pitches();

  @JsonProperty("STRIKES")
  Optional<Integer> strikes();

  @JsonProperty("ERA")
  Optional<String> era();
}
