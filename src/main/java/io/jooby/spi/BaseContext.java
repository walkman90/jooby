package io.jooby.spi;

import io.jooby.Context;
import io.jooby.Route;
import io.jooby.Router;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.Executor;

public abstract class BaseContext implements Context, Route.Chain {

  private Route route;

  private final Iterator<Route> router;

  protected Route.After after;

  public BaseContext(Iterator<Route> router) {
    this.router = router;
  }

  @Override public Context after(Route.After after) {
    if (this.after == null) {
      this.after = after;
    } else {
      this.after = this.after.then(after);
    }
    return this;
  }

  @Nonnull @Override public final Route.Chain chain() {
    return this;
  }

  @Override public final void next(Context ctx) throws Throwable {
    if (committed()) {
      return;
    }
    if (router.hasNext()) {
      this.route = router.next();
      this.route.handler.handle(ctx, this);
    } else {
      throw new IllegalStateException("NOT FOUND");
    }
  }

  @Nonnull @Override public final Route route() {
    return route;
  }

  @Override public final Context dispatch(Executor executor) {
    if (isInIoThread()) {
      throw new Context.Dispatched(executor);
    }
    return this;
  }
}
