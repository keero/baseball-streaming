package org.sundbybergheat.baseballstreaming.models.wbsc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = PlateCountImpl.class)
@JsonDeserialize(as = PlateCountImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface PlateCount {
  int id();

  int type();

  String label();

  String pitch();

  List<Integer> coords();
}
