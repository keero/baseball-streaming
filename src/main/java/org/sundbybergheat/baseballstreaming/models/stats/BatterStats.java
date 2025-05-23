package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.jetbrains.annotations.Nullable;

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

  @Nullable
  Integer doubles();

  @Nullable
  Integer triples();

  @Nullable
  Integer homeruns();

  int runsBattedIn();

  @Nullable
  Integer totalBases();

  String battingAverage();

  @Nullable
  String slugging();

  @Nullable
  String onBasePercentage();

  @Nullable
  String onBasePercentagePlusSlugging();

  @Nullable
  Integer walks();

  @Nullable
  Integer hitByPitch();

  @Nullable
  Integer strikeouts();

  @Nullable
  Integer groundoutDoublePlay();

  @Nullable
  Integer sacrificeFlies();

  @Nullable
  Integer sacrificeHits();

  @Nullable
  Integer stolenBases();

  @Nullable
  Integer caughtStealing();
}
