package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = StatsDataSetImpl.class)
@JsonDeserialize(as = StatsDataSetImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface StatsDataSet {
  ElementRS elementRS();
}
