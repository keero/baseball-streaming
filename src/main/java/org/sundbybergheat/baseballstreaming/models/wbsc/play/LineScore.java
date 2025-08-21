package org.sundbybergheat.baseballstreaming.models.wbsc.play;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.vavr.control.Either;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record LineScore(
    @JsonProperty("awayruns") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Either<List<String>, Map<String, String>> awayRuns,
    @JsonProperty("awaytotals") Optional<Totals> awayTotals,
    @JsonProperty("homeruns") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Either<List<String>, Map<String, String>> homeRuns,
    @JsonProperty("hometotals") Optional<Totals> homeTotals) {}
