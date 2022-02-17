package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = SeriesStatsImpl.class)
@JsonDeserialize(as = SeriesStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface SeriesStats {
  int year();

  @Default
  default boolean otherSeries() {
    return false;
  }

  String seriesName();

  Optional<String> teamFlagUrl();

  Optional<String> playerImageUrl();

  Optional<BatterStats> batting();

  Optional<PitcherStats> pitching();
}
