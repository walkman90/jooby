package io.jooby;

import io.jooby.internal.RouterImpl;
import io.jooby.netty.Netty;
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

  public Logger log() {
    return LoggerFactory.getLogger(getClass());
  }

}
