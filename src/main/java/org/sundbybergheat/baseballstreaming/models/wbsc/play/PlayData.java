package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayData(
    @JsonProperty("t") String timestamp,
    @JsonProperty("p") String pitcher,
    @JsonProperty("n") String text,
    @JsonProperty("b") String batter,
    @JsonProperty("a") String atBat,
    @JsonProperty("i") String inning) {}
