package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PlateCount(
    @JsonProperty("id") String id,
    @JsonProperty("type") String type,
    @JsonProperty("label") String label,
    @JsonProperty("pitch") String pitch,
    @JsonProperty("coords") List<String> coords) {}
