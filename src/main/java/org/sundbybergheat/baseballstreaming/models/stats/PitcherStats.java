package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PitcherStatsImpl.class)
@JsonDeserialize(as = PitcherStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface PitcherStats {

  String playerId();

  String teamId();

  int wins();

  int losses();

  String era();

  int appearances();

  int gamesStarted();

  int saves();

  int completeGames();

  int shutouts();

  String inningsPitched();

  int hitsAllowed();

  int runsAllowed();

  int earnedRunsAllowed();

  int walksAllowed();

  int strikeouts();

  int doublesAllowed();

  int triplesAllowed();

  int homerunsAllowed();

  int atBats();

  String opponentBattingAverage();

  int wildPitches();

  int hitByPitch();

  int balks();

  int sacrificeFliesAllowed();

  int sacrificeHitsAllowed();

  int groundOuts();

  int flyOuts();
}
