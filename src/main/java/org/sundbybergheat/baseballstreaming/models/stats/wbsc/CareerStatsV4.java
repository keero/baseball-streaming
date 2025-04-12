package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = CareerStatsV4Impl.class)
@JsonDeserialize(as = CareerStatsV4Impl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface CareerStatsV4 {
  Map<String, BattingStats> batting();

  Map<String, PitchingStats> pitching();

  TotalsRow totalsRow();
}
