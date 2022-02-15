package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = AnimationImpl.class)
@JsonDeserialize(as = AnimationImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface Animation {
  int batter();

  int strike();

  int ball();
}
