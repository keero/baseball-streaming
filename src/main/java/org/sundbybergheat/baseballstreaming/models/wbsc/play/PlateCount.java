package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Optional;

public record PlateCount(
    @JsonProperty("id") Optional<String> id,
    @JsonProperty("type") Optional<String> type,
    @JsonProperty("label") Optional<String> label,
    @JsonProperty("pitch") Optional<String> pitch,
    @JsonProperty("coords") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> coords) {}
