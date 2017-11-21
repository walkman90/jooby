package io.jooby.router;

public class GreedySuffix extends Segment {
  private final String suffix;

  public GreedySuffix(String suffix) {
    this.suffix = suffix;
  }

  @Override public int matches(String segment, int offset) {
    return segment.lastIndexOf(suffix);
  }
}
