package apps;

import io.jooby.App;
import io.jooby.AppTest;
import io.jooby.Context;
import io.jooby.Route;
import io.jooby.Router;
import io.jooby.WebClient;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import net.bytebuddy.implementation.bytecode.Throw;
import org.jooby.funzy.Throwing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

@AppTest
public class DetachTest extends App {

  {
    get("/rx", detach(ctx -> {
      Flowable.fromCallable(() -> "Hello rx!")
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.single())
          .subscribe(ctx::send);
    }));

    get("/rx/flow", rx(ctx ->
        Flowable.fromCallable(() -> "Hello rx/flow!")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.single())
    ));
  }

  @Test
  public void rxJava(WebClient client) throws IOException {
    client.get("/rx", rsp -> {
      assertEquals("9", rsp.header("content-length"));
      assertEquals("Hello rx!", rsp.body().string());
    });
  }

  @Test
  public void rxJavaFlow(WebClient client) throws IOException {
    client.get("/rx/flow", rsp -> {
      assertEquals("14", rsp.header("content-length"));
      assertEquals("Hello rx/flow!", rsp.body().string());
    });
  }

  public static <T> Throwing.Consumer2<Context, Flowable<T>> rx2() {
    return (ctx, flow) -> {
      flow.subscribe(it -> ctx.send(it.toString()));
    };
  }

  public static <T> Route.Handler rx(Throwing.Function<Context, Flowable<T>> handler) {
    return new Route.Handler() {
      @Override public void handle(@Nonnull Context ctx, @Nonnull Route.Chain chain)
          throws Throwable {
        ctx.detach();
        handler.apply(ctx)
            .subscribe(success -> ctx.send(success.toString()));
      }

      @Override public Object handle(@Nonnull Context ctx) throws Throwable {
        return ctx;
      }
    };
  }
}
