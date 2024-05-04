package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.jetbrains.annotations.Nullable;

@Immutable
@JsonSerialize(as = SituationImpl.class)
@JsonDeserialize(as = SituationImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Situation {
  String inning();

  String uniform();

  String gender();

  String batter();

  @JsonProperty("batterid")
  String batterId();

  @Nullable
  Arm bats();

  String batting();

  String avg();

  String pitcher();

  @JsonProperty("pitcherid")
  String pitcherId();

  @JsonProperty("pitcherthrows")
  @Nullable
  Arm pitcherThrows();

  @JsonProperty("pitcherera")
  String pitcherERA();

  @JsonProperty("pitcherip")
  String pitcherIP();

  int runner1();

  int runner2();

  int runner3();

  int outs();

  int balls();

  int strikes();

  @JsonProperty("currentinning")
  String currentInning();
}
