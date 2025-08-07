package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record PlayerSeasonStats(
    @JsonProperty("PA") Optional<String> plateAppearances,
    @JsonProperty("AB") Optional<String> atBats,
    @JsonProperty("H") Optional<String> hits,
    @JsonProperty("BB") Optional<String> walks,
    @JsonProperty("DOUBLE") Optional<String> doubles,
    @JsonProperty("TRIPLE") Optional<String> triples,
    @JsonProperty("HR") Optional<String> homeruns,
    @JsonProperty("SF") Optional<String> sacFlies,
    @JsonProperty("HBP") Optional<String> hitByPitch,
    @JsonProperty("PITCHER") Optional<String> earnedRuns,
    @JsonProperty("PITCHOUTS") Optional<String> outs,
    @JsonProperty("PITCHBB") Optional<String> pitcherWalks,
    @JsonProperty("PITCHSO") Optional<String> pitcherStrikeouts) {}
