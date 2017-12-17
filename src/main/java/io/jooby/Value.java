package io.jooby;

import javax.annotation.Nonnull;
import java.util.List;

public interface Value {

  @Nonnull List<String> toList();

  @Nonnull String value();
}
