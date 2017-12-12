package apps;

import io.jooby.App;

public class HelloWorld extends App {
  {
    get("/", ctx -> "Hello Jooby");

    get("/dispatch", ctx -> {
      return ctx.dispatch().send((String) null);
    });
  }

  public static void main(String[] args) {
    new HelloWorld().start();
  }
}
