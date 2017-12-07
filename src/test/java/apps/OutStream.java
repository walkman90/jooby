package apps;

import io.jooby.App;

import java.io.Writer;

public class OutStream extends App {

  {
    get("/stream", ctx -> {
      try (Writer writer = ctx.toWriter()) {
        writer.write("Hi ");
        writer.write("from ");
        writer.write("OutputStream");
        writer.write("!");
      }
      return ctx.end();
    });
  }

  public static void main(String[] args) {
    new OutStream().start();
  }
}
