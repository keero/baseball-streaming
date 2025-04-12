package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStats;

@Immutable
@JsonSerialize(as = CategoryStatsImpl.class)
@JsonDeserialize(as = CategoryStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface CategoryStats {
  Category category();

  CareerStats careerStats();
}
