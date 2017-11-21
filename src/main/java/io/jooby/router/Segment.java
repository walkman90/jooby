package io.jooby.router;

public abstract class Segment {

  Segment next;

  public int matches(String segment) {
    return matches(segment, 0);
  }

  public abstract int matches(String segment, int offset);

  public Segment next(Segment next) {
    this.next = next;
    return next;
  }

  public static Segment empty() {
    return new Segment() {
      @Override public int matches(String segment, int offset) {
        return -1;
      }
    };
  }
}
