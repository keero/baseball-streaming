package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collections;
import java.util.Map;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = AllStatsImpl.class)
@JsonDeserialize(as = AllStatsImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface AllStats {
  @Default
  default Map<String, SeriesStats> seriesStats() {
    return Collections.emptyMap();
  }
}
