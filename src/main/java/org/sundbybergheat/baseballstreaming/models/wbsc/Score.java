package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = ScoreImpl.class)
@JsonDeserialize(as = ScoreImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Score {
  @JsonProperty("R")
  int runs();

  @JsonProperty("H")
  int hits();

  @JsonProperty("E")
  int errors();

  @JsonProperty("LOB")
  int leftOnBase();
}
