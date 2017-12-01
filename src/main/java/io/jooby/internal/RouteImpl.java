package io.jooby.internal;

import io.jooby.Route;
import static java.util.Objects.requireNonNull;

public class RouteImpl implements Route {

  public final String method;

  public final PathPattern pattern;

  public final Route.Filter handler;

  RouteImpl next;

  public RouteImpl(String method, String pattern, Route.Filter handler) {
    this.method = requireNonNull(method, "Method required.").toUpperCase();
    this.pattern = new PathPattern(requireNonNull(pattern, "Pattern required."), false);
    this.handler = requireNonNull(handler, "Filter required.");
  }

  @Override public String method() {
    return method;
  }

  @Override public String pattern() {
    return pattern.pattern();
  }

  @Override public Filter handler() {
    return handler;
  }

}
