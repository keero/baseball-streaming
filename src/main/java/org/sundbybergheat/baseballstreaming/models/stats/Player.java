package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PlayerImpl.class)
@JsonDeserialize(as = PlayerImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Player {
  int id();

  int number();

  String name();
}
