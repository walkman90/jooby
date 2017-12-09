package io.jooby;

public interface WebServer {

  void start(int port, Router router, boolean join);

  void stop();
}
