package io.jooby;

import org.jooby.funzy.Throwing;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public interface Router {

  String ALL = "*";

  String GET = "GET";

  String POST = "POST";

  String PUT = "PUT";

  String DELETE = "DELETE";

  String PATCH = "PATCH";

  default @Nonnull Route before(@Nonnull String method, @Nonnull String pattern, @Nonnull Route.Before handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default @Nonnull Route before(@Nonnull String pattern, @Nonnull Route.Before handler) {
    return define(ALL, pattern, handler);
  }

  default @Nonnull Route after(@Nonnull String method, @Nonnull String pattern, @Nonnull Route.After handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default @Nonnull Route after(@Nonnull String pattern, @Nonnull Route.After handler) {
    return define(ALL, pattern, handler);
  }

  default @Nonnull Route get(@Nonnull String pattern, @Nonnull Route.Handler handler) {
    return define(GET, pattern, handler);
  }

  default @Nonnull Route post(@Nonnull String pattern, @Nonnull Route.Handler handler) {
    return define(POST, pattern, handler);
  }

  default @Nonnull Route put(@Nonnull String pattern, @Nonnull Route.Handler handler) {
    return define(PUT, pattern, handler);
  }

  default @Nonnull Route delete(@Nonnull String pattern, @Nonnull Route.Handler handler) {
    return define(DELETE, pattern, handler);
  }

  default @Nonnull Route patch(@Nonnull String pattern, @Nonnull Route.Handler handler) {
    return define(PATCH, pattern, handler);
  }

  default @Nonnull Route.Handler detach(@Nonnull Throwing.Consumer<Context> handler) {
    return new Route.Handler() {
      @Override public void handle(@Nonnull Context ctx, @Nonnull Route.Pipeline chain)
          throws Throwable {
        ctx.detach();
        handler.accept(ctx);
      }

      @Override public Object handle(@Nonnull Context ctx) throws Throwable {
        return ctx;
      }
    };
  }

  @Nonnull Route.Pipeline pipeline(@Nonnull String method, @Nonnull String path);

  @Nonnull Stream<Route> routes();

  @Nonnull Route define(@Nonnull String method, @Nonnull String pattern, @Nonnull Route.Filter handler);

  @Nonnull Router err(@Nonnull Route.ErrHandler handler);

  @Nonnull Route.ErrHandler err();
}
