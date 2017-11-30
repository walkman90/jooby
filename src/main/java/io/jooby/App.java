package io.jooby;

import io.jooby.internal.router.RouterImpl;
import io.jooby.netty.Netty;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.jooby.funzy.Throwing;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class App implements Router {

  private Router router = new RouterImpl();

  public void start() {
    Netty server = new Netty();
    server.start(router, true);
  }

  @Override public Iterator iterator(String method, String path) {
    return router.iterator(method, path);
  }

  @Override public Route define(String method, String pattern, Route.Filter handler) {
    return router.define(method, pattern, handler);
  }

  public static void main(String[] args) {
    App app = new App();

    app.before("GET","/filters", ctx -> {
      System.out.println("1>" + Thread.currentThread());
    });

    app.before("GET","/filters", ctx -> {
      System.out.println("2>" + Thread.currentThread());
    });

    app.after("GET","/filters", ctx -> {
      System.out.println("3>" + Thread.currentThread());
    });

    app.after("GET","/filters", ctx -> {
      System.out.println("4>" + Thread.currentThread());
    });

    app.get("/", ctx -> ctx.send("Hello Jooby 2"));
    app.get("/favicon.ico", ctx -> {
      return ctx.send(new byte[0]);
    });

    app.get("/bytes", ctx -> {
      byte[] bytes = "bytes".getBytes(StandardCharsets.UTF_8);
      ctx.length(bytes.length * 2);
      return ctx.write(bytes);
    });
    app.get("/bytes", ctx -> {
      byte[] bytes = "bytes".getBytes(StandardCharsets.UTF_8);
      return ctx.write(bytes).end();
    });

    app.get("/chunk", ctx -> {
      ctx.dispatch(null);
      return ctx.write("Hello", StandardCharsets.UTF_8);
    });
    app.get("/chunk", ctx -> ctx.write(" chunk from ", StandardCharsets.UTF_8));
    app.get("/chunk", ctx -> ctx.write(Thread.currentThread().getName(), StandardCharsets.UTF_8));
    app.get("/chunk", ctx -> ctx.write("!", StandardCharsets.UTF_8).end());

    app.get("/worker", ctx -> {
      ctx.dispatch();
      return ctx.send("Foo" + Thread.currentThread().getName(), StandardCharsets.UTF_8);
    });

    app.get("/rx", rx(ctx ->
        Flowable.fromCallable(() -> "RxJava")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(v -> ctx.send("&& " + v + " from " + Thread.currentThread()))
    ));

    app.get("/filters", ctx -> ctx.dispatch().send("Filters!"));

    app.start();
  }

  public static Route.Handler rx(Route.Handler handler) {
    return new Route.Handler() {
      @Override public void handle(Context ctx, Route.Chain chain) throws Throwable {
        Object result = handler.handle(ctx);
        if (result instanceof Flowable) {
          ((Flowable) result).subscribe(v -> ctx.send(v + " from " + Thread.currentThread()));
        }
      }

      @Override public Object handle(Context ctx) throws Throwable {
        return null;
      }
    };
  }
}
