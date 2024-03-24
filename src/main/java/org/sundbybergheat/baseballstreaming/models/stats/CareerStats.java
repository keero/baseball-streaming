package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = CareerStatsImpl.class)
@JsonDeserialize(as = CareerStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface CareerStats {
  Stats stats();

  Total total();
}
