package io.jooby.spi;

import io.jooby.Context;
import io.jooby.Route;
import io.jooby.Value;
import io.jooby.internal.ValueImpl;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

public abstract class BaseContext implements Context {

  private Route route;

  protected Route.After after;

  @Override public Context after(Route.After after) {
    if (this.after == null) {
      this.after = after;
    } else {
      this.after = this.after.then(after);
    }
    return this;
  }

  public Value query(String name) {
    return new ValueImpl(query().values(name));
  }

  @Nonnull @Override public final Route route() {
    return route;
  }

  @Nonnull @Override public Context route(@Nonnull Route route) {
    this.route = route;
    return this;
  }

  @Override public final Context dispatch(Executor executor) {
    if (isInIoThread()) {
      throw new Context.Dispatched(executor);
    }
    return this;
  }
}
