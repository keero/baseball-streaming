package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Animation(
    @JsonProperty("batter") String batter,
    @JsonProperty("strike") String strike,
    @JsonProperty("ball") String ball) {}
