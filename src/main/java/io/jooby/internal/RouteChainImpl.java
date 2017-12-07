package io.jooby.internal;

import io.jooby.Context;
import io.jooby.Route;

public class RouteChainImpl implements Route.Chain {
  private final RouteImpl[] routes;
  private int r;

  public RouteChainImpl(RouteImpl[] routes) {
    this.routes = routes;
  }

  @Override public void next(Context ctx) throws Throwable {
    if (ctx.committed()) {
      return;
    }
    if (r < routes.length) {
      RouteImpl route = routes[r++];
      ctx.route(route);
      route.handler.handle(ctx, this);
    }
  }
}
