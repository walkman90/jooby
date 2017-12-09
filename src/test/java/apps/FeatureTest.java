package apps;

import io.jooby.App;
import io.jooby.AppTest;
import io.jooby.Context;
import io.jooby.Route;
import io.jooby.WebClient;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.jooby.funzy.Throwing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;

@AppTest
public class FeatureTest extends App {

  {
    before(GET, "/filters", ctx -> {
      ctx.header("one", 1);
    });

    before(GET, "/filters", ctx -> {
      ctx.header("two", 2);
    });

    after(GET, "/filters", ctx -> {
      ctx.header("three", 3);
    });

    after(GET, "/filters", ctx -> {
      ctx.header("four", 4);
    });

    get("/", ctx -> "Hello world!");

    get("/chunk", ctx -> {
      return ctx.write("Hello ")
          .write("chunks")
          .write("!")
          .end();
    });

    get("/writer", ctx -> ctx.writer(writer -> {
      writer.write("Hello ");
      writer.write("writer");
      writer.write("!");
    }));

    get("/filters", ctx -> "Hello filters!");

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
  public void chunks(WebClient client) throws IOException {
    client.get("/chunk", rsp -> {
      assertEquals("chunked", rsp.header("transfer-encoding"));
      assertEquals(null, rsp.header("content-length"));
      assertEquals("Hello chunks!", rsp.body().string());
    });
  }

  @Test
  public void writer(WebClient client) throws IOException {
    client.get("/writer", rsp -> {
      assertEquals("chunked", rsp.header("transfer-encoding"));
      assertEquals(null, rsp.header("content-length"));
      assertEquals("Hello writer!", rsp.body().string());
    });
  }

  @Test
  public void filters(WebClient client) throws IOException {
    client.get("/filters", rsp -> {
      assertEquals(null, rsp.header("transfer-encoding"));
      assertEquals("14", rsp.header("content-length"));
      assertEquals("1", rsp.header("one"));
      assertEquals("2", rsp.header("two"));
      assertEquals("3", rsp.header("three"));
      assertEquals("4", rsp.header("four"));
      assertEquals("Hello filters!", rsp.body().string());
    });
  }

  @Test
  public void helloWorld(WebClient client) throws IOException {
    client.get("/", rsp -> {
      assertEquals(null, rsp.header("transfer-encoding"));
      assertEquals("12", rsp.header("content-length"));
      assertEquals("Hello world!", rsp.body().string());
    });
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
