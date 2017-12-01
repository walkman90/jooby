package io.jooby;

public interface Route {

  interface Chain {
    void next(Context ctx) throws Throwable;
  }

  interface Filter {
    void handle(Context ctx, Chain chain) throws Throwable;
  }

  interface Before extends Filter {
    default void handle(Context ctx, Chain chain) throws Throwable {
      handle(ctx);
      chain.next(ctx);
    }

    void handle(Context ctx) throws Throwable;
  }

  interface After extends Filter {
    default void handle(Context ctx, Chain chain) throws Throwable {
      ctx.after(this);
      chain.next(ctx);
    }

    default After then(After next) {
      return ctx -> {
        handle(ctx);
        next.handle(ctx);
      };
    }

    void handle(Context ctx) throws Throwable;
  }

  interface Handler extends Filter {
    default void handle(Context ctx, Chain chain) throws Throwable {
      Object result = handle(ctx);
      if (!ctx.committed()) {
        ctx.send(result.toString());
      }
    }

    Object handle(Context ctx) throws Throwable;
  }

  String method();

  String pattern();

  Filter handler();

  static String normalize(String pattern) {
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
