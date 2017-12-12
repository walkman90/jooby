package io.jooby.internal;

import io.jooby.Context;
import io.jooby.Err;
import io.jooby.Route;
import org.jooby.funzy.Throwing;
import static org.jooby.funzy.Throwing.isFatal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteChainImpl implements Route.Chain {
  private final RouteImpl[] routes;
  private final Route.ErrHandler err;
  private int r;

  public RouteChainImpl(RouteImpl[] routes, Route.ErrHandler err) {
    this.routes = routes;
    this.err = err;
  }

  @Override public void next(Context ctx) {
    if (ctx.committed() || ctx.isDetached()) {
      return;
    }
    if (r < routes.length) {
      RouteImpl route = routes[r++];
      ctx.route(route);
      try {
        route.handler.handle(ctx, this);
      } catch (Context.Dispatched d) {
        r -= 1;
        throw d;
      } catch (Throwable x) {
        handleError(ctx, x);
      }
    }
  }

  private void handleError(Context ctx, Throwable x) {
    try {
      Err err = x instanceof Err ? (Err) x : new Err(500, x);
      this.err.handle(ctx, err);
    } catch (Throwable errx) {
      Logger log = LoggerFactory.getLogger(Err.class);
      log.error("execution of error handler resulted in a new exception", errx);
    }
    // rethrow fatal exception
    if (isFatal(x)) {
      throw Throwing.sneakyThrow(x);
    }
  }
}
