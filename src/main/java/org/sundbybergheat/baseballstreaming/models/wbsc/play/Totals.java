package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record Totals(
    @JsonProperty("R") Optional<String> runs,
    @JsonProperty("H") Optional<String> hits,
    @JsonProperty("E") Optional<String> errors,
    @JsonProperty("LOB") Optional<String> leftOnBase) {}
