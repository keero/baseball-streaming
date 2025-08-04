package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Totals(
    @JsonProperty("R") String runs,
    @JsonProperty("H") String hits,
    @JsonProperty("E") String errors,
    @JsonProperty("LOB") String leftOnBase) {}
