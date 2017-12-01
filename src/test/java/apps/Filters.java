package apps;

import io.jooby.App;

public class Filters extends App {

  {
    before("GET","/filters", ctx -> {
      System.out.println("1>" + Thread.currentThread());
    });

    before("GET","/filters", ctx -> {
      System.out.println("2>" + Thread.currentThread());
    });

    after("GET","/filters", ctx -> {
      System.out.println("3>" + Thread.currentThread());
    });

    after("GET","/filters", ctx -> {
      System.out.println("4>" + Thread.currentThread());
    });

    get("/filters", ctx -> ctx.dispatch().send("Filters!"));
  }

  public static void main(String[] args) {
    new Filters().start();
  }
}
