package io.jooby;

public interface WebServer {

  default void start(Router router) {
    start(router, true);
  }

  void start(Router router, boolean join);

}
