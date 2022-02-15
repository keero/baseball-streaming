package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = BatterStatsImpl.class)
@JsonDeserialize(as = BatterStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface BatterStats {
  String playerId();

  String teamId();

  int games();

  int atBats();

  int runs();

  int hits();

  int doubles();

  int triples();

  int homeruns();

  int runsBattedIn();

  int totalBases();

  String battingAverage();

  String slugging();

  String onBasePercentage();

  String onBasePercentagePlusSlugging();

  int walks();

  int hitByPitch();

  int strikeouts();

  int groundoutDoublePlay();

  int sacrificeFlies();

  int sacrificeHits();

  int stolenBases();

  int caughtStealing();
}
