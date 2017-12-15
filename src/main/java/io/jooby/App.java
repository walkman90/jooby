package io.jooby;

import io.jooby.internal.RouterImpl;
import io.jooby.netty.Netty;
import org.jooby.funzy.Throwing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class App implements Router {

  private RouterImpl router = new RouterImpl();
  private WebServer server;

  public void start() {
    start(8080, true);
  }

  public void start(int port, boolean join) {
    this.server = new Netty();
    log().info("\n\n    http://localhost:{}\n", port);
    server.start(port, router.start(), join);
  }

  public void stop() {
    this.server.stop();
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

  @Override public <T> Router with(Throwing.Consumer2<Context, T> consumer, Runnable action) {
    return router.with(consumer, action);
  }

  @Override public Route.ErrHandler err() {
    return router.err();
  }

  @Override public App err(Route.ErrHandler handler) {
    router.err(handler);
    return this;
  }

  public Logger log() {
    return LoggerFactory.getLogger(getClass());
  }

}
