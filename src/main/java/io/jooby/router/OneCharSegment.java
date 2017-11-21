package io.jooby.router;

public class OneCharSegment extends Segment {
  @Override public int matches(String segment, int offset) {
    int next = offset + 1;
    return next <= segment.length() ? next : -1;
  }

  @Override public String toString() {
    return "?";
  }
}
