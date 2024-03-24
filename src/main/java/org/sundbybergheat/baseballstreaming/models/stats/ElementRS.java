package org.sundbybergheat.baseballstreaming.models.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@JsonSerialize(as = ElementRSImpl.class)
@JsonDeserialize(as = ElementRSImpl.class)
@Style(jdkOnly = true, typeImmutable = "*Impl")
public interface ElementRS {
  List<Category> categories();
}
