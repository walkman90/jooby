package io.jooby.internal;

import io.jooby.Route;
import io.jooby.Router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RouterImpl implements Router {

  private static final int _10 = 10;
  private RouteImpl[] routes = new RouteImpl[_10];
  private int routeSize;
  private ConcurrentHashMap<Integer, RouteImpl[]> cache = new ConcurrentHashMap<>();

  public Route.Chain chain(String method, String path) {
    return new RouteChainImpl(chainArray(cache, routes, method, path));
  }

  @Override public Stream<Route> routes() {
    return Arrays.stream(routes);
  }

  public RouterImpl start() {
    if (routeSize < routes.length) {
      // shrink route array:
      routes = Arrays.copyOf(routes, routeSize);
    }
    return this;
  }

  private static RouteImpl[] chainArray(ConcurrentHashMap<Integer, RouteImpl[]> cache,
      RouteImpl[] routes, String method, String path) {
    return cache.computeIfAbsent(hash(method, path), (Integer key) -> {
      List<RouteImpl> matches = new ArrayList<>(_10);
      for (int r = 0; r < routes.length; r++) {
        RouteImpl it = routes[r];
        PathPattern.Result result = it.pattern.test(path);
        if (result.matches) {
          matches.add(it);
        }
      }
      int size = matches.size();
      if (size > 0) {
        RouteImpl route = matches.get(size - 1);
        if (route.endpoint) {
          return matches.toArray(new RouteImpl[matches.size()]);
        }
      }
      return new RouteImpl[]{RouteImpl
          .fallback(null, method, path, fallback(path, routes))};
    });
  }

  private static Route.Filter fallback(String path, RouteImpl[] nodes) {
    return (ctx, chain) -> {
      if (path.equals("/favicon.ico")) {
        // zero/empty response
        ctx.length(0).end();
      } else {
        throw new RuntimeException("NOT FOUND");
      }
    };
  }

  @Override public Route define(String method, String pattern, Route.Filter handler) {
    RouteImpl route = new RouteImpl(method, pattern, handler);
    if (routeSize >= routes.length) {
      routes = Arrays.copyOf(routes, routeSize + _10);
    }
    routes[routeSize++] = route;
    return route;
  }

  private static Integer hash(String method, String path) {
    return Integer.valueOf(31 * (31 + method.hashCode()) + path.hashCode());
  }
}
