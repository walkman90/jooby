package io.jooby;

import org.jooby.funzy.Throwing;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public interface Router {

  String GET = "GET";

  String POST = "POST";

  String PUT = "PUT";

  String DELETE = "DELETE";

  String PATCH = "PATCH";

  default String[] methods() {
    return new String[]{GET, POST, PUT, PATCH};
  }

  default Route before(String method, String pattern, Route.Before handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default Route before(String pattern, Route.Before handler) {
    return define("*", pattern, handler);
  }

  default Route after(String method, String pattern, Route.After handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default Route after(String pattern, Route.After handler) {
    return define("*", pattern, handler);
  }

  default Route get(String pattern, Route.Handler handler) {
    return define(GET, pattern, handler);
  }

  default Route post(String pattern, Route.Handler handler) {
    return define(POST, pattern, handler);
  }

  default Route put(String pattern, Route.Handler handler) {
    return define(PUT, pattern, handler);
  }

  default Route delete(String pattern, Route.Handler handler) {
    return define(DELETE, pattern, handler);
  }

  default Route patch(String pattern, Route.Handler handler) {
    return define(PATCH, pattern, handler);
  }

  default Route.Handler detach(Throwing.Consumer<Context> handler) {
    return new Route.Handler() {
      @Override public void handle(@Nonnull Context ctx, @Nonnull Route.Chain chain)
          throws Throwable {
        ctx.detach();
        handler.accept(ctx);
      }

      @Override public Object handle(@Nonnull Context ctx) throws Throwable {
        return ctx;
      }
    };
  }

  Route.Chain chain(String method, String path);

  Stream<Route> routes();

  Route define(String method, String pattern, Route.Filter handler);

  Router err(Route.ErrHandler handler);

  Route.ErrHandler err();

  <T> Router with(Throwing.Consumer2<Context, T> consumer, Runnable action);
}
