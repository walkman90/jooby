package io.jooby.router;

public final class ZeroOrMore extends Segment {

  @Override public int matches(String segment, int offset) {
    int slashIndex = segment.indexOf('/', offset);
    return slashIndex == -1 ? segment.length() : slashIndex;
  }

  @Override public String toString() {
    return "*";
  }
}
