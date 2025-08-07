package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record Situation(
    @JsonProperty("inning") Optional<String> inning,
    @JsonProperty("uniform") Optional<String> uniform,
    @JsonProperty("gender") Optional<String> gender,
    @JsonProperty("batter") Optional<String> batter,
    @JsonProperty("batterid") Optional<String> batterId,
    @JsonProperty("bats") Optional<String> bats,
    @JsonProperty("batting") Optional<String> batting,
    @JsonProperty("avg") Optional<String> avg,
    @JsonProperty("pitcher") Optional<String> pitcher,
    @JsonProperty("pitcherid") Optional<String> pitcherId,
    @JsonProperty("pitcherthrows") Optional<String> pitcherThrows,
    @JsonProperty("pitcherera") Optional<String> pitcherERA,
    @JsonProperty("pitcherip") Optional<String> pitcherIP,
    @JsonProperty("runner1") Optional<String> runnerOnFirst,
    @JsonProperty("runner2") Optional<String> runnerOnSecond,
    @JsonProperty("runner3") Optional<String> runnerOnThird,
    @JsonProperty("outs") Optional<String> outs,
    @JsonProperty("balls") Optional<String> balls,
    @JsonProperty("strikes") Optional<String> strikes,
    @JsonProperty("extrainnings") Optional<String> extraInnings,
    @JsonProperty("currentinning") Optional<String> currentInning) {}
