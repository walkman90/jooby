package io.jooby.internal;

import io.jooby.Route;
import static java.util.Objects.requireNonNull;

public class RouteImpl implements Route {

  public final String method;

  public final PathPattern pattern;

  private final String patternString;

  public final Route.Filter handler;

  public final boolean endpoint;

  public RouteImpl(String method, String pattern, Route.Filter handler) {
    this.method = method.toUpperCase();
    this.pattern = new PathPattern(pattern, false);
    this.patternString = this.pattern.pattern();
    this.handler = handler;
    this.endpoint = handler instanceof Route.Handler;
  }

  private RouteImpl(String name, String method, String pattern, Route.Filter handler) {
    this.method = method;
    this.pattern = null;
    this.patternString = pattern;
    this.handler = handler;
    this.endpoint = true;
  }

  final static RouteImpl fallback(String name, String method, String pattern,
      Route.Filter handler) {
    return new RouteImpl(name, method, pattern, handler);
  }

  @Override public String method() {
    return method;
  }

  @Override public String pattern() {
    return patternString;
  }

  @Override public Filter handler() {
    return handler;
  }
}
