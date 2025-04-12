package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Map;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = CareerStatsV2Impl.class)
@JsonDeserialize(as = CareerStatsV2Impl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface CareerStatsV2 {
  Map<String, BattingStats> batting();

  List<PitchingStats> pitching();

  TotalsRow totalsRow();
}
