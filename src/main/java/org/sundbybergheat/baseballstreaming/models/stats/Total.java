package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = TotalImpl.class)
@JsonDeserialize(as = TotalImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Total {
  String battingTotal();

  String fieldingTotal();

  String pitchingTotal();
}
