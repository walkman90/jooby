package apps;

import io.jooby.App;

public class Chunks extends App {

  {
    get("/chunk", ctx -> ctx.write(" chunk from ")
        .write(Thread.currentThread().getName())
        .write("!")
        .end());
  }

  public static void main(String[] args) {
    new Chunks().start();
  }
}
