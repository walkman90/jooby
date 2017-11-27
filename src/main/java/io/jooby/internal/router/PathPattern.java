package io.jooby.router;

import io.jooby.Route;
import static java.lang.Character.toLowerCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PathPattern {

  public static final class Result {
    public final boolean matches;
    public final Map<String, String> variables;

    public Result(boolean matches, Map<String, String> variables) {
      this.matches = matches;
      this.variables = variables;
    }
  }

  private static final Result NO_MATCH = new Result(false, Collections.emptyMap());

  private static final Map<String, String> NO_VARS_MAP = Collections.emptyMap();

  private static final List<String> NO_VARS = Collections.emptyList();

  private final Matcher[] matcher;

  private final String pattern;

  private List<String> variables = NO_VARS;

  public PathPattern(String pattern) {
    this(pattern, false);
  }

  public PathPattern(String pattern, boolean ignoreCase) {
    this.pattern = Route.normalize(pattern);
    this.matcher = parse(this.pattern, ignoreCase, variable -> {
      if (variables == NO_VARS) {
        variables = new ArrayList<>();
      }
      variables.add(variable);
    });
  }

  public String pattern() {
    return pattern;
  }

  public List<String> variables() {
    return Collections.unmodifiableList(variables);
  }

  public Result test(String path) {
    int offset = 0;
    int i = 0;
    int len = path.length();
    Map<String, String> vars = variables == NO_VARS ? NO_VARS_MAP : new HashMap<>();
    while (offset != -1 && i < matcher.length) {
      offset = matcher[i++].matches(path, offset, len, vars);
    }
    if (offset == len) {
      return new Result(true, vars);
    }
    return NO_MATCH;
  }

  private static final Matcher[] parse(String segment, boolean ignoreCase,
      Consumer<String> variables) {
    StringBuilder value = new StringBuilder();
    List<Matcher> segments = new ArrayList<>();
    int len = segment.length();
    for (int i = 0; i < len; ) {
      char ch = segment.charAt(i);
      int nextIndex = i + 1;
      if (ch == '?') {
        /** Add any pending trailing segment: */
        staticSegment(value, ignoreCase, segments);
        segments.add(OneChar.INSTANCE);
      } else if (ch == '*') {
        /** Add any pending trailing segment: */
        staticSegment(value, ignoreCase, segments);
        if (i + 1 < len && '*' == segment.charAt(i + 1)) {
          int start = i + 2;
          int slashIndex = segment.indexOf('/', start);
          if (slashIndex == -1) {
            slashIndex = segment.length();
          }
          String variable = segment.substring(start, slashIndex);
          if (variable.length() == 0) {
            segments.add(Greedy.INSTANCE);
          } else {
            variables.accept(variable);
            segments.add(new GreedyNamed(variable));
          }
          nextIndex = start + variable.length();
        } else {
          if (chartAt(segment, i - 1) == '/' && chartAt(segment, i + 1) == '/') {
            segments.add(OneSegment.INSTANCE);
          } else {
            segments.add(ZeroOrMore.INSTANCE);
          }
        }
      } else if (ch == ':') {
        /**
         * Handle /:name expression at the end or limited by '/' (/:name/)
         */
        nextIndex = segment.indexOf('/', i);
        if (nextIndex == -1) {
          nextIndex = segment.length();
        }
        if (chartAt(segment, i + 1) == '/') {
          // not a variable, something like :/
          value.append(ch);
        } else {
          /** Add any pending trailing segment: */
          staticSegment(value, ignoreCase, segments);

          // /:name vs /:name/
          int slashIndex = segment.indexOf('/', nextIndex);
          String variable = segment.substring(i + 1, nextIndex);
          variables.accept(variable);
          if (slashIndex == -1) {
            // /:name
            segments.add(new VarTail(variable));
          } else {
            // /:name/
            segments.add(new Var(variable));
          }
        }
      } else if (ch == '{') {
        /** Add any pending trailing segment: */
        staticSegment(value, ignoreCase, segments);

        /**
         * Handle /{name} expression at the end or middle of path segment (/{name}/).
         * Also, handle regular expressions /{name:regex}
         */
        nextIndex = segment.indexOf('}', i);
        if (nextIndex == -1) {
          throw new IllegalArgumentException(
              "Invalid pattern. Unclosed variable: " + segment.substring(i) + ", expecting '}'");
        }
        int colon = segment.indexOf(':', i);
        int slashIndex = segment.indexOf('/', nextIndex);
        if (colon == -1) {
          // /{name}, /{name}/ or /{name}suffix
          String variable = segment.substring(i + 1, nextIndex);
          variables.accept(variable);

          if (slashIndex == -1) {
            int suffixLen = len - nextIndex - 1;
            if (suffixLen > 0) {
              // /{name}suffix
              segments.add(new VarSuffixTail(variable, suffixLen));
            } else {
              // /{name}
              segments.add(new VarTail(variable));
            }
          } else {
            int suffixLen = slashIndex - nextIndex - 1;
            if (suffixLen > 0) {
              // /{name}/
              segments.add(new VarSuffix(variable, suffixLen));
            } else {
              // /{name}/
              segments.add(new Var(variable));
            }
          }
        } else {
          String variable = segment.substring(i + 1, colon);
          variables.accept(variable);

          // /{name:regex} or /{name:regex}/
          Pattern pattern = Pattern.compile(segment.substring(colon + 1, nextIndex));
          if (slashIndex == -1) {
            segments.add(new RegexTail(variable, pattern));
          } else {
            segments.add(new Regex(variable, pattern));
          }
        }
        nextIndex += 1;
      } else {
        if (ch == '/') {
          /** Add a new static matcher if we reach a / */
          staticSegment(value, ignoreCase, segments);
        }
        value.append(ch);
      }
      i = nextIndex;
    }
    /** Add any pending trailing segment: */
    staticSegment(value, ignoreCase, segments);
    return finalize(segments);
  }

  private static void staticSegment(StringBuilder value, boolean ignoreCase,
      List<Matcher> segments) {
    if (value.length() > 0) {
      if (ignoreCase) {
        segments.add(new StaticNoCase(value.toString()));
      } else {
        segments.add(new Static(value.toString()));
      }
      value.setLength(0);
    }
  }

  private static Matcher[] finalize(List<Matcher> segments) {
    for (int i = 1; i < segments.size(); i++) {
      Matcher it = segments.get(i);
      Matcher prev = segments.get(i - 1);
      if (it instanceof Static) {
        String suffix = ((Static) it).value;
        if (prev instanceof Greedy) {
          segments.set(i - 1, new GreedySuffix(suffix));
        } else if (prev instanceof ZeroOrMore) {
          segments.set(i - 1, new ZeroOrMoreSuffix(suffix));
        } else if (prev instanceof GreedyNamed) {
          segments.set(i - 1, new GreedySuffixNamed(((GreedyNamed) prev).name, suffix));
        }
      } else if (it instanceof Greedy || it instanceof GreedyNamed) {
        if (prev instanceof Static) {
          // /foo/bar/**
          segments.set(i - 1, null);
        }
      }
    }
    return segments.stream()
        .filter(Objects::nonNull)
        .toArray(Matcher[]::new);
  }

  private static final char chartAt(String segment, int pos) {
    if (pos >= 0 && pos < segment.length()) {
      return segment.charAt(pos);
    }
    return 0;
  }

  private interface Matcher {
    int matches(String segment, int offset, int length, Map<String, String> variables);
  }

  private static final class Greedy implements Matcher {

    private static final Greedy INSTANCE = new Greedy();

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      return length;
    }
  }

  private static final class GreedyNamed implements Matcher {

    final String name;

    GreedyNamed(String name) {
      this.name = name;
    }

    @Override public final int matches(String segment, int offset, int len,
        Map<String, String> variables) {
      int from = offset + 1;
      if (from < len) {
        variables.put(name, segment.substring(from));
      }
      return len;
    }
  }

  private static final class GreedySuffix implements Matcher {
    private final String suffix;

    GreedySuffix(String suffix) {
      this.suffix = suffix;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      return segment.lastIndexOf(suffix);
    }
  }

  private static final class GreedySuffixNamed implements Matcher {
    private final String suffix;

    private final String name;

    GreedySuffixNamed(String name, String suffix) {
      this.name = name;
      this.suffix = suffix;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int end = segment.lastIndexOf(suffix);
      if (end != -1) {
        int start = offset + 1;
        if (start < end) {
          variables.put(name, segment.substring(start, end));
        }
      }
      return end;
    }
  }

  private static final class OneChar implements Matcher {

    static final OneChar INSTANCE = new OneChar();

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int next = offset + 1;
      return next <= length ? next : -1;
    }
  }

  private static final class OneSegment implements Matcher {

    static final OneSegment INSTANCE = new OneSegment();

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      return segment.indexOf('/', offset);
    }
  }

  private static final class Regex implements Matcher {

    private final String name;
    private final Pattern pattern;

    Regex(String name, Pattern pattern) {
      this.name = name;
      this.pattern = pattern;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int slashIndex = segment.indexOf('/', offset);
      if (slashIndex == -1) {
        return -1;
      }
      String value = segment.substring(offset, slashIndex);
      java.util.regex.Matcher matcher = pattern.matcher(value);
      if (matcher.matches()) {
        variables.put(name, value);
        return slashIndex;
      }
      return -1;
    }
  }

  private static final class RegexTail implements Matcher {

    private final String name;
    private final Pattern pattern;

    RegexTail(String name, Pattern pattern) {
      this.name = name;
      this.pattern = pattern;
    }

    @Override public final int matches(String segment, int offset, int len,
        Map<String, String> variables) {
      String value = segment.substring(offset);
      if (offset < len) {
        java.util.regex.Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
          variables.put(name, value);
          return len;
        }
      }
      return -1;
    }
  }

  private final static class Static implements Matcher {

    private final String value;
    private final int len;

    Static(String value) {
      this.value = value;
      this.len = value.length();
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int len = length - offset;
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
  }

  private static final class StaticNoCase implements Matcher {
    final String value;
    private final int len;

    StaticNoCase(String value) {
      this.value = value;
      this.len = value.length();
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int len = length - offset;
      if (len < this.len) {
        return -1;
      }
      int i = 0;
      while (i < this.len) {
        char v1 = value.charAt(i);
        char v2 = segment.charAt(offset + i);
        if (v1 != v2 && toLowerCase(v1) != toLowerCase(v2)) {
          return -1;
        }
        i += 1;
      }
      return offset + i;
    }
  }

  private static final class Var implements Matcher {

    private final String name;

    Var(String name) {
      this.name = name;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int slashIndex = segment.indexOf('/', offset);
      if (slashIndex != -1) {
        String value = segment.substring(offset, slashIndex);
        variables.put(name, value);
      }
      return slashIndex;
    }
  }

  private static final class VarSuffix implements Matcher {

    private final String name;
    private final int suffixLen;

    VarSuffix(String name, int suffixLen) {
      this.name = name;
      this.suffixLen = suffixLen;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int slashIndex = segment.indexOf('/', offset);
      if (slashIndex != -1) {
        slashIndex -= suffixLen;
        String value = segment.substring(offset, slashIndex);
        variables.put(name, value);
      }
      return slashIndex;
    }
  }

  private static final class VarSuffixTail implements Matcher {

    private final String name;
    private final int suffixLen;

    VarSuffixTail(String name, int suffixLen) {
      this.name = name;
      this.suffixLen = suffixLen;
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int len = length - suffixLen;
      if (offset < len) {
        String value = segment.substring(offset, len);
        variables.put(name, value);
        return len;
      }
      return -1;
    }
  }

  private static final class VarTail implements Matcher {

    private final String name;

    VarTail(String name) {
      this.name = name;
    }

    @Override public final int matches(String segment, int offset, int len,
        Map<String, String> variables) {
      if (offset < len) {
        String value = segment.substring(offset, len);
        variables.put(name, value);
        return len;
      }
      return -1;
    }
  }

  private static final class ZeroOrMore implements Matcher {

    static final ZeroOrMore INSTANCE = new ZeroOrMore();

    @Override public int matches(String segment, int offset, int length,
        Map<String, String> variables) {
      int slashIndex = segment.indexOf('/', offset);
      return slashIndex == -1 ? length : slashIndex;
    }
  }

  private static final class ZeroOrMoreSuffix implements Matcher {

    private final String suffix;

    private final int suffixlen;

    ZeroOrMoreSuffix(String suffix) {
      this.suffix = suffix;
      this.suffixlen = suffix.length();
    }

    @Override public final int matches(String segment, int offset, int length,
        Map<String, String> variables) {
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
  }
}
