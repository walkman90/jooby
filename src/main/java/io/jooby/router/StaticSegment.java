package io.jooby.router;

public final class StaticSegment extends Segment {

  final String value;
  private final int len;

  public StaticSegment(String value) {
    this.value = value;
    this.len = value.length();
  }

  @Override public int matches(String segment, int offset) {
    int len = segment.length() - offset;
    if (len < this.len) {
      return -1;
    }
    int i = 0;
    while (i < this.len) {
      if (value.charAt(i) != segment.charAt(offset + i)) {
        return -1;
      }
      i += 1;
    }
    return offset + i;
  }

  @Override public String toString() {
    return value;
  }
}
