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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@AppTest
public class DispatchTest extends App {

  {
    get("/def-worker", ctx -> {
      /**
       * Dispatch to default executor:
       */
      ctx.dispatch();

      return Thread.currentThread().getName();
    });

    get("/custom-worker", ctx -> {
      /**
       * Dispatch to custom executor:
       */
      ctx.dispatch(ForkJoinPool.commonPool());

      return Thread.currentThread().getName();
    });

    before("/pipe/**", ctx -> {
      ctx.header("p1", "1:" + ctx.isInIoThread());
    });

    before("/pipe/2", ctx -> {
      ctx.dispatch();
      ctx.header("p2", "2:" + ctx.isInIoThread());
    });

    before("/pipe/**", ctx -> {
      ctx.header("p3", "3:" + ctx.isInIoThread());
    });

    get("/pipe/:id", ctx -> {
      ctx.header("p4", "4:" + ctx.isInIoThread());
      return "pipe-with-dispatch";
    });
  }

  @Test
  public void dispathToDefaultWorker(WebClient client) throws IOException {
    client.get("/def-worker", rsp -> {
      assertTrue(rsp.body().string().toLowerCase().startsWith("defaulteventexecutor"));
    });
  }

  @Test
  public void dispathToCustomWorker(WebClient client) throws IOException {
    client.get("/custom-worker", rsp -> {
      assertTrue(rsp.body().string().toLowerCase().startsWith("forkjoinpool"));
    });
  }

  @Test
  public void ableToDispatchWillExecutingAPipeline(WebClient client) throws IOException {
    client.get("/pipe/2", rsp -> {
      assertEquals("1:true", rsp.header("p1"));
      assertEquals("2:false", rsp.header("p2"));
      assertEquals("3:false", rsp.header("p3"));
      assertEquals("4:false", rsp.header("p4"));
      assertEquals("pipe-with-dispatch", rsp.body().string());
    });
  }
}
