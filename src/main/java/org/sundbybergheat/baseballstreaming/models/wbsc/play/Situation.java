package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Situation(
    @JsonProperty("inning") String inning,
    @JsonProperty("uniform") String uniform,
    @JsonProperty("gender") String gender,
    @JsonProperty("batter") String batter,
    @JsonProperty("batterid") String batterId,
    @JsonProperty("bats") String bats,
    @JsonProperty("batting") String batting,
    @JsonProperty("avg") String avg,
    @JsonProperty("pitcher") String pitcher,
    @JsonProperty("pitcherid") String pitcherId,
    @JsonProperty("pitcherthrows") String pitcherThrows,
    @JsonProperty("pitcherera") String pitcherERA,
    @JsonProperty("pitcherip") String pitcherIP,
    @JsonProperty("runner1") String runnerOnFirst,
    @JsonProperty("runner2") String runnerOnSecond,
    @JsonProperty("runner3") String runnerOnThird,
    @JsonProperty("outs") String outs,
    @JsonProperty("balls") String balls,
    @JsonProperty("strikes") String strikes,
    @JsonProperty("extrainnings") String extraInnings,
    @JsonProperty("currentinning") String currentInning) {}
