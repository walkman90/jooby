package io.jooby.router;

import java.util.ArrayList;
import java.util.List;

public class PathPattern {

  private final Segment compiled;

  public PathPattern(String pattern) {
    compiled = parse(pattern);
  }

  public boolean matches(String path) {
    int offset = 0;
    Segment next = compiled;
    int len = path.length();
    while (offset != -1 && next != null) {
      offset = next.matches(path, offset);
      next = next.next;
    }
    return offset == len;
  }

  private static final Segment parse(String segment) {
    StringBuilder value = new StringBuilder();
    List<Segment> segments = new ArrayList<>();
    int len = segment.length();
    for (int i = 0; i < len; ) {
      char ch = segment.charAt(i);
      int move = i + 1;
      if (ch == '?') {
        if (value.length() > 0) {
          segments.add(new StaticSegment(value.toString()));
          value.setLength(0);
        }
        segments.add(new OneCharSegment());
      } else if (ch == '*') {
        if (value.length() > 0) {
          segments.add(new StaticSegment(value.toString()));
          value.setLength(0);
        }
        if (i + 1 < len && '*' == segment.charAt(i + 1)) {
          move = i + 2;
          segments.add(new Greedy());
        } else {
          if (ch(segment, i - 1) == '/' && ch(segment, i + 1) == '/') {
            segments.add(new OneSegment());
          } else {
            segments.add(new ZeroOrMore());
          }
        }
      } else if (ch == ':') {
        move = segment.indexOf('/', i);
        if (move == i + 1) {
          // not a variable, something like :/
          value.append(ch);
        } else {
          if (move == -1) {
            move = segment.length();
          }
          if (value.length() > 0) {
            segments.add(new StaticSegment(value.toString()));
            value.setLength(0);
          }
          segments.add(new ZeroOrMore());
        }
      } else if (ch == '{') {
        move = segment.indexOf('}', i);
        if (move == -1) {
          throw new IllegalArgumentException("Invalid pattern " + segment);
        }
        segments.add(new ZeroOrMore());
      } else {
        if (ch == '/') {
          if (value.length() > 0) {
            segments.add(new StaticSegment(value.toString()));
            value.setLength(0);
          }
        }
        value.append(ch);
      }
      i = move;
    }
    if (value.length() > 0) {
      //      if (segments.size() > 0 && segments.get(segments.size() - 1) instanceof ZeroOrMore) {
      //      }
      segments.add(new StaticSegment(value.toString()));
      value.setLength(0);
    }
    return optimize(segments);
  }

  private static Segment optimize(List<Segment> segments) {
    for (int i = 1; i < segments.size(); i++) {
      Segment it = segments.get(i);
      Segment prev = segments.get(i - 1);
      if (it instanceof StaticSegment) {
        String suffix = ((StaticSegment) it).value;
        if (prev instanceof Greedy) {
          segments.set(i - 1, new GreedySuffix(suffix));
        } else if (prev instanceof ZeroOrMore) {
          //          if (!suffix.equals("/")) {
          segments.set(i - 1, new ZeroOrMoreSuffix(suffix));
          //          }
        }
      } else if (it instanceof Greedy) {
        if (prev instanceof StaticSegment) {
          // /foo/bar/**
          segments.set(i - 1, null);
        }
      }
    }
    return link(segments);
  }

  private static Segment link(List<Segment> segments) {
    Segment prev = segments.get(0);
    Segment root = prev;
    for (int i = 1; i < segments.size(); i++) {
      Segment it = segments.get(i);
      if (it != null) {
        if (prev != null) {
          prev.next(it);
        }
        prev = it;
        if (root == null) {
          root = it;
        }
      }
    }
    return root;
  }

  private static char ch(String segment, int pos) {
    if (pos >= 0 && pos < segment.length()) {
      return segment.charAt(pos);
    }
    return 0;
  }

  private static String suffix(String segment, int offset) {
    StringBuilder buff = new StringBuilder();
    for (int i = offset; i < segment.length(); i++) {
      char ch = segment.charAt(i);
      if (ch == '?' || ch == '*' || ch == '/' || ch == ':' || ch == '{') {
        break;
      }
      buff.append(ch);
    }
    return buff.toString();
  }

  @Override public String toString() {
    return compiled.toString();
  }
}
