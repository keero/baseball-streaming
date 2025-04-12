package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = CareerStatsImpl.class)
@JsonDeserialize(as = CareerStatsImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface CareerStats {
  List<BattingStats> batting();

  List<PitchingStats> pitching();

  TotalsRow totalsRow();
}
