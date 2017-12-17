package io.jooby;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface QueryString {
  @Nonnull Stream<String> names();

  @Nonnull List<String> values(@Nonnull String name);

  @Nonnull String raw();

  default @Nonnull Map<String, List<String>> toMap() {
    Map<String, List<String>> map = new LinkedHashMap<>();
    names().forEach(n -> map.put(n, values(n)));
    return map;
  }
}
