package io.jooby.router;

public class Greedy extends Segment {
  @Override public int matches(String segment, int offset) {
    return segment.length();
  }
}
