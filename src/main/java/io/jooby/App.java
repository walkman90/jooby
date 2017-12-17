package io.jooby;

import io.jooby.internal.RouterImpl;
import io.jooby.netty.Netty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class App implements Router {

  private RouterImpl router = new RouterImpl();
  private WebServer server;

  public void start() {
    start(8080, true);
  }

  public void start(int port, boolean join) {
    this.server = new Netty();
    RouterImpl router = this.router.start();
    log().info(
        getClass().getSimpleName() + " ready:\n" + router.toString() + "\n\nhttp://localhost:" + port + "\n");
    server.start(port, router, join);
  }

  public void stop() {
    this.server.stop();
  }

  @Override public Stream<Route> routes() {
    return router.routes();
  }

  @Override public Route.Pipeline pipeline(String method, String path) {
    return router.pipeline(method, path);
  }

  @Override public Route define(String method, String pattern, Route.Filter handler) {
    return router.define(method, pattern, handler);
  }

  @Override public Route.ErrHandler err() {
    return router.err();
  }

  @Override public App err(Route.ErrHandler handler) {
    router.err(handler);
    return this;
  }

  public @Nonnull Logger log() {
    return LoggerFactory.getLogger(getClass());
  }

}
