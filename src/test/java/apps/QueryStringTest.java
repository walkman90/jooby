package apps;

import io.jooby.App;
import io.jooby.AppTest;
import io.jooby.WebClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@AppTest
public class QueryStringTest extends App {
  {
    get("/", ctx -> ctx.query().raw());

    get("/queryStringMap", ctx -> ctx.query().toMap());

    get("/search", ctx -> ctx.query("q").value());

    get("/searchList", ctx -> ctx.query("q").toList());
  }

  @Test
  public void queryString(WebClient client) throws IOException {
    client.get("/?", rsp -> {
      assertEquals("", rsp.body().string());
    });

    client.get("/?q", rsp -> {
      assertEquals("q", rsp.body().string());
    });

    client.get("/?foo=1&bar=2", rsp -> {
      assertEquals("foo=1&bar=2", rsp.body().string());
    });
  }

  @Test
  public void queryStringMap(WebClient client) throws IOException {
    client.get("/queryStringMap?", rsp -> {
      assertEquals("{}", rsp.body().string());
    });

    client.get("/queryStringMap?q", rsp -> {
      assertEquals("{q=[]}", rsp.body().string());
    });

    client.get("/queryStringMap?foo=1&bar=2", rsp -> {
      assertEquals("{foo=[1], bar=[2]}", rsp.body().string());
    });
  }

  @Test
  public void queryParam(WebClient client) throws IOException {
    client.get("/search?q=find something", rsp -> {
      assertEquals("find something", rsp.body().string());
    });

    client.get("/searchList?q=foo&q=bar", rsp -> {
      assertEquals("[foo, bar]", rsp.body().string());
    });
  }
}
