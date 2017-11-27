package io.jooby.internal.router;

import io.jooby.Route;
import io.jooby.Router;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RouterImpl implements Router {

  private static class RouteIterator implements Iterator {
    private final Routes r;
    private final String path;
    private Route route;
    private int routeIndex;

    RouteIterator(Routes r, String path) {
      this.r = r;
      this.path = path;
    }

    @Override public boolean hasNext() {
      while (routeIndex < r.length) {
        route = r.routes[routeIndex++];
        PathPattern.Result result = route.pattern.test(path);
        if (result.matches) {
          return true;
        }
      }
      return false;
    }

    @Override public Route next() {
      return route;
    }
  }

  private static class Routes {
    private int length;
    private Route[] routes = new Route[10];

    public void add(Route route) {
      if (length >= routes.length) {
        Route[] tmp = routes;
        routes = new Route[length + 5];
        System.arraycopy(tmp, 0, routes, 0, length);
      }
      routes[length++] = route;
    }
  }

  private Map<String, Routes> routes = new HashMap<>();

  @Override public Iterator iterator(String method, String path) {
    Routes r = this.routes.get(method);
    return new RouteIterator(r, path);
  }

  @Override public Route define(String method, String pattern, Route.Handler handler) {
    Route route = new Route(method, pattern, handler);
    routes.computeIfAbsent(route.method, k -> new Routes())
        .add(route);
    return route;
  }
}
