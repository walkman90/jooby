package io.jooby;

import io.jooby.internal.router.PathPattern;
import static java.util.Objects.requireNonNull;

public final class Route {

  public interface Chain {
    void next(Context ctx) throws Throwable;
  }

  public interface Filter {
    void handle(Context ctx, Chain chain) throws Throwable;
  }

  public interface Handler extends Filter {
    default void handle(Context ctx, Chain chain) throws Throwable {
      Object result = handle(ctx);
      if (ctx.committed()) {
        return;
      }
      if (result == null || ctx == result) {
        chain.next(ctx);
      } else {
        ctx.send(result.toString());
      }
    }

    Object handle(Context ctx) throws Throwable;
  }

  public final String method;

  public final PathPattern pattern;

  public final Route.Handler handler;

  public Route(String method, String pattern, Route.Handler handler) {
    this.method = requireNonNull(method, "Method required.").toUpperCase();
    this.pattern = new PathPattern(requireNonNull(pattern, "Pattern required."), false);
    this.handler = requireNonNull(handler, "Handler required.");
  }

  public static final String normalize(String pattern) {
    if (pattern.equals("*")) {
      return "/**";
    }
    StringBuilder buff = new StringBuilder();
    char prev = '/';
    buff.append(prev);
    for (int i = 0; i < pattern.length(); i++) {
      char ch = pattern.charAt(i);
      if (ch != '/' || prev != '/') {
        buff.append(ch);
      }
      prev = ch;
    }
    int last = buff.length() - 1;
    if (last > 1 && buff.charAt(last) == '/') {
      buff.setLength(last);
    }
    if (buff.length() == pattern.length()) {
      return pattern;
    }
    return buff.toString();
  }
}
