package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = SeriesIdImpl.class)
@JsonDeserialize(as = SeriesIdImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface SeriesId {
  String id();

  Optional<Integer> year();

  Optional<String> prefix();

  Optional<String> postfix();
}
