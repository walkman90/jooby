package io.jooby.internal;

import io.jooby.Context;
import io.jooby.Route;

import java.util.Iterator;

public class RouteChainImpl implements Route.Chain {
  private final Iterator<Route> router;

  public RouteChainImpl(Iterator<Route> router) {
    this.router = router;
  }

  @Override public void next(Context ctx) throws Throwable {
    if (ctx.committed()) {
      return;
    }
    if (router.hasNext()) {
      router.next().handler().handle(ctx, this);
    } else {
      throw new IllegalStateException("NOT FOUND");
    }
  }
}
