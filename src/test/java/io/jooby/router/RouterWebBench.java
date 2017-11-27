package io.jooby.router;

import io.jooby.internal.router.RouterImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;

import java.io.IOException;

public class RouterWebBench extends BaseBenchmark {

  private RouterImpl tr;

  @Setup
  public void setup() {
//    tr = new RouterImpl();
//    tr.add(("/"));
//
//    tr.add(("/favicon.ico"));
//
//    tr.add(("/pages/*"));
//    tr.add(("/pages/*"));
//
//    tr.add(("/article"));
//    tr.add(("/article/"));
//    tr.add(("/article/near"));
//    tr.add(("/article/{id}"));
//    tr.add(("/article/{id}"));
//    tr.add(("/article/@{id}"));
//    tr.add(("/article/{id}/{opts}"));
//
//    tr.add(("/article/{iffd}/edit"));
//    tr.add(("/article/{id}/related"));
//    tr.add(("/article/slug/{month}/-/{day}/{year}"));
//
//    tr.add(("/admin/user"));
//    tr.add(("/admin/user/"));
//    tr.add(("/admin/user/{id}"));
//    tr.add(("/admin/apps/{id}"));
//    tr.add(("/admin/**ff"));
//    tr.add(("/admin/apps/{id}/**ff"));
//    tr.add(("/admin/apps/{id}"));
//
//    tr.add(("/hubs/{hubID}/view"));
//    tr.add(("/hubs/{hubID}/view/*"));
//    tr.add(("/hubs/{hubID}/view/**page"));
//    tr.add(("/hubs/{hubID}/users"));
//
//    tr.add(("/users/super/**f"));
//    tr.add(("/users/**f"));
//
//    tr.prepare();
  }

  @Benchmark
  public void benchmark() throws IOException {
//    tr.matches(("/"));
//
//    tr.matches(("/favicon.ico"));
//
//    tr.matches(("/pages/"));
//    tr.matches(("/pages/yes"));
//
//    tr.matches(("/article"));
//    tr.matches(("/article/"));
//    tr.matches(("/article/near"));
//    tr.matches(("/article/neard"));
//    tr.matches(("/article/123"));
//    tr.matches(("/article/@peter"));
//    tr.matches(("/article/123/456"));
//
//    tr.matches(("/article/111/edit"));
//    tr.matches(("/article/22/related"));
//    tr.matches(("/article/slug/sept/-/4/2015"));
//
//    tr.matches(("/admin/user"));
//    tr.matches(("/admin/user/"));
//    tr.matches(("/admin/user/1"));
//    tr.matches(("/admin/apps/33"));
//    tr.matches(("/admin/lots/of/:fun"));
//    tr.matches(("/admin/apps/333/woot"));
//    tr.matches(("/admin/apps/333"));
//
//    tr.matches(("/hubs/123/view"));
//    tr.matches(("/hubs/123/view/index.html"));
//    tr.matches(("/hubs/123/view/index.html"));
//    tr.matches(("/hubs/123/users"));
//
//    tr.matches(("/users/super/123/okay/yes"));
//    tr.matches(("/users/123/okay/yes"));
  }
}
