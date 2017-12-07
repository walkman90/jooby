package io.jooby;

import io.jooby.internal.RouterImpl;
import io.jooby.netty.Netty;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class App implements Router {

  private RouterImpl router = new RouterImpl();

  public void start() {
    Netty server = new Netty();
    server.start(router.start(), true);
  }

  @Override public Stream<Route> routes() {
    return router.routes();
  }

  @Override public Route.Chain chain(String method, String path) {
    return router.chain(method, path);
  }

  @Override public Route define(String method, String pattern, Route.Filter handler) {
    return router.define(method, pattern, handler);
  }
}
