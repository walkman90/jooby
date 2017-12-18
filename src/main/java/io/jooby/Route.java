package io.jooby;

import javax.annotation.Nonnull;

public interface Route {

  interface Pipeline {
    void resume(@Nonnull Context ctx);

    void next(@Nonnull Context ctx);
  }

  interface Filter {
    void handle(@Nonnull Context ctx, @Nonnull Pipeline chain) throws Throwable;
  }

  interface Before extends Filter {
    default void handle(@Nonnull Context ctx, @Nonnull Pipeline chain) throws Throwable {
      handle(ctx);
      chain.next(ctx);
    }

    void handle(@Nonnull Context ctx) throws Throwable;
  }

  interface After extends Filter {
    default void handle(@Nonnull Context ctx, @Nonnull Pipeline chain) throws Throwable {
      ctx.after(this);
      chain.next(ctx);
    }

    default @Nonnull After then(@Nonnull After next) {
      return ctx -> {
        handle(ctx);
        next.handle(ctx);
      };
    }

    void handle(@Nonnull Context ctx) throws Throwable;
  }

  interface Handler extends Filter {
    default void handle(@Nonnull Context ctx, @Nonnull Pipeline chain) throws Throwable {
      Object result = handle(ctx);
      if (!ctx.committed()) {
        if (result != ctx) {
          ctx.send(result.toString());
        } else {
          chain.next(ctx);
        }
      }
    }

    Object handle(@Nonnull Context ctx) throws Throwable;
  }

  interface ErrHandler {
    void handle(@Nonnull Context ctx, @Nonnull Err problem);

    default @Nonnull ErrHandler then(@Nonnull Route.ErrHandler next) {
      return (ctx, problem) -> {
        handle(ctx, problem);
        if (!ctx.committed()) {
          next.handle(ctx, problem);
        }
      };
    }
  }

  @Nonnull String method();

  @Nonnull String pattern();

  @Nonnull Filter handler();

  default void handle(@Nonnull Context ctx, @Nonnull Pipeline chain) throws Throwable {
    handler().handle(ctx, chain);
  }

  static @Nonnull String normalize(@Nonnull String pattern) {
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
