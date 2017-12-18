package apps;

import io.jooby.App;

public class HelloWorld extends App {

  {
    get("/", ctx -> ctx.type("text/plain").send("Hello World!"));
  }

  public static void main(String[] args) {
    new HelloWorld().start();
  }
}
