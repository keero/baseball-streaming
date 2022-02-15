package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PlayDataImpl.class)
@JsonDeserialize(as = PlayDataImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface PlayData {
  @JsonProperty("t")
  Long timestamp();

  @JsonProperty("p")
  String pitcher();

  @JsonProperty("n")
  String text();

  @JsonProperty("b")
  String batter();

  @JsonProperty("a")
  String atBat();

  String r1();

  String r2();

  @JsonProperty("i")
  String inning();
}
