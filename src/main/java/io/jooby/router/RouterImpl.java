package io.jooby.router;

import java.util.ArrayList;
import java.util.List;

public class RouterImpl {
  private List<PathPattern> patterns = new ArrayList<>();

  private PathPattern[] array;

  public RouterImpl add(String pattern) {
    patterns.add(new PathPattern(pattern));
    return this;
  }

  public RouterImpl prepare() {
    array = patterns.toArray(new PathPattern[patterns.size()]);
    return this;
  }

  public boolean matches(String path) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].test(path).matches) {
        return true;
      }
    }
    return false;
  }
}
