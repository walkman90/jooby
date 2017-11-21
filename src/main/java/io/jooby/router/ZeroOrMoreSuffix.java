package io.jooby.router;

public final class ZeroOrMoreSuffix extends Segment {

  private final String suffix;
  private final int suffixlen;

  public ZeroOrMoreSuffix(String suffix) {
    this.suffix = suffix;
    this.suffixlen = suffix.length();
  }

  @Override public int matches(String segment, int offset) {
    int slashIndex = segment.indexOf('/', offset);
    if (slashIndex == -1) {
      return segment.lastIndexOf(suffix);
    }
    if (suffixlen == 1 && suffix.charAt(0) == '/') {
      return slashIndex;
    }
    int index = segment.substring(offset, slashIndex).lastIndexOf(suffix);
    return index == -1 ? index : offset + index;
  }

  @Override public String toString() {
    return "*";
  }
}
