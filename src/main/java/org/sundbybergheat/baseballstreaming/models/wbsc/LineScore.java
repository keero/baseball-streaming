package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = LineScoreImpl.class)
@JsonDeserialize(as = LineScoreImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface LineScore {

  @JsonProperty("awayruns")
  @AllowNulls
  List<Integer> awayRuns();

  @JsonProperty("awaytotals")
  Score awayTotals();

  @JsonProperty("homeruns")
  @AllowNulls
  List<Integer> homeRuns();

  @JsonProperty("hometotals")
  Score homeTotals();
}
