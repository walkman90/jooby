package io.jooby.internal;

import io.jooby.Context;
import io.jooby.Err;
import io.jooby.Route;
import org.jooby.funzy.Throwing;
import static org.jooby.funzy.Throwing.isFatal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineImpl implements Route.Pipeline {
  private final RouteImpl[] routes;
  private final Route.ErrHandler err;
  private int r;

  public PipelineImpl(RouteImpl[] routes, Route.ErrHandler err) {
    this.routes = routes;
    this.err = err;
  }

  public void resume(Context ctx) {
    r -= 1;
    next(ctx);
  }

  @Override public void next(Context ctx) {
    if (ctx.committed()) {
      return;
    }
    if (r < routes.length) {
      RouteImpl route = routes[r++];
      ctx.route(route);
      try {
        route.handler.handle(ctx, this);
      } catch (Context.Dispatched dispatched) {
        throw dispatched;
      } catch (Throwable x) {
        handleError(ctx, x);
      }
    } else {
      ctx.end();
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
