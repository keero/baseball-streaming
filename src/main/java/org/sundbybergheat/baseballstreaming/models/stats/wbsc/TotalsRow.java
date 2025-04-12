package org.sundbybergheat.baseballstreaming.models.stats.wbsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = TotalsRowImpl.class)
@JsonDeserialize(as = TotalsRowImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface TotalsRow {
  Optional<BattingStats> batting();

  Optional<PitchingStats> pitching();
}
