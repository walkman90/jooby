package io.jooby;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jooby.funzy.Throwing;

public class WebClient {

  public class Request {
    private final okhttp3.Request.Builder req;

    public Request(okhttp3.Request.Builder req) {
      this.req = req;
    }

    public Request prepare(Throwing.Consumer<okhttp3.Request.Builder> configurer) {
      configurer.accept(req);
      return this;
    }

    public void execute(Throwing.Consumer<Response> callback) {
      try (Response rsp = client.newCall(req.build()).execute()) {
        callback.accept(rsp);
      } catch (Throwable x) {
        throw Throwing.sneakyThrow(x);
      }
    }
  }

  private final int port;
  private OkHttpClient client = new OkHttpClient();

  public WebClient(int port) {
    this.port = port;
  }

  public Request get(String path) {
    okhttp3.Request.Builder req = new okhttp3.Request.Builder();
    req.url("http://localhost:" + port + path);
    return new Request(req);
  }

  public void get(String path, Throwing.Consumer<Response> callback) {
    get(path).execute(callback);
  }
}
