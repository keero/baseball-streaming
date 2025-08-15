package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record Animation(
    @JsonProperty("batter") Optional<String> batter,
    @JsonProperty("strike") Optional<String> strike,
    @JsonProperty("ball") Optional<String> ball) {}
