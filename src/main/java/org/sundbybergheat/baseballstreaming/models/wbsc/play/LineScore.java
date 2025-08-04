package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LineScore(
    @JsonProperty("awayruns") List<String> awayRuns,
    @JsonProperty("awaytotals") Totals awayTotals,
    @JsonProperty("homeruns") List<String> homeRuns,
    @JsonProperty("hometotals") Totals homeTotals) {}
