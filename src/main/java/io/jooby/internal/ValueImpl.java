package io.jooby.internal;

import io.jooby.Err;
import io.jooby.Status;
import io.jooby.Value;

import java.util.List;

public final class ValueImpl implements Value {

  private final List<String> values;

  public ValueImpl(List<String> values) {
    this.values = values;
  }

  public List<String> toList() {
    return values;
  }

  public String value() {
    if (values.size() == 0) {
      throw new Err(Status.BAD_REQUEST);
    }
    return values.get(0);
  }
}
