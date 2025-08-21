package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record PlayData(
    @JsonProperty("t") Optional<String> timestamp,
    @JsonProperty("p") Optional<String> pitcher,
    @JsonProperty("n") Optional<String> text,
    @JsonProperty("b") Optional<String> batter,
    @JsonProperty("a") Optional<String> atBat,
    @JsonProperty("i") Optional<String> inning) {}
