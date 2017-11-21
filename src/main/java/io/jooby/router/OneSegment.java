package io.jooby.router;

public final class OneSegment extends Segment {

  @Override public int matches(String segment, int offset) {
    return segment.indexOf('/', offset);
  }

  @Override public String toString() {
    return "*";
  }
}
