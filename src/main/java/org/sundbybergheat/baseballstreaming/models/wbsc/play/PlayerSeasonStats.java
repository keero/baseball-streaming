package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerSeasonStats(
    @JsonProperty("PA") String plateAppearances,
    @JsonProperty("AB") String atBats,
    @JsonProperty("H") String hits,
    @JsonProperty("BB") String walks,
    @JsonProperty("DOUBLE") String doubles,
    @JsonProperty("TRIPLE") String triples,
    @JsonProperty("HR") String homeruns,
    @JsonProperty("SF") String sacFlies,
    @JsonProperty("HBP") String hitByPitch,
    @JsonProperty("PITCHER") String earnedRuns,
    @JsonProperty("PITCHOUTS") String outs,
    @JsonProperty("PITCHBB") String pitcherWalks,
    @JsonProperty("PITCHSO") String pitcherStrikeouts) {}
