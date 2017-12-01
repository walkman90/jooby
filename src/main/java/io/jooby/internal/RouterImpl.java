package io.jooby.internal;

import io.jooby.Route;
import io.jooby.Router;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RouterImpl implements Router {

  private static class RouteIterator implements Iterator {
    private RouteImpl route;
    private RouteImpl it;
    private final String path;

    RouteIterator(RouteImpl r, String path) {
      this.route = r;
      this.it = r;
      this.path = path;
    }

    @Override public boolean hasNext() {
      while (it != null) {
        PathPattern.Result result = it.pattern.test(path);
        if (result != PathPattern.NO_MATCH) {
          route = it;
          it = it.next;
          return true;
        } else {
          it = it.next;
        }
      }
      return false;
    }

    @Override public Route next() {
      return route;
    }
  }

  private static class Node {
    RouteImpl first;
    RouteImpl next;
  }

  private Map<String, Node> hash = new HashMap<>();

  @Override public Iterator iterator(String method, String path) {
    Node node = this.hash.get(method);
    return new RouteIterator(node.first, path);
  }

  @Override public Route define(String method, String pattern, Route.Filter handler) {
    RouteImpl route = new RouteImpl(method, pattern, handler);
    Node node = hash.get(route.method);
    if (node == null) {
      node = new Node();
      node.first = route;
      hash.put(route.method, node);
    } else {
      node.next.next = route;
    }
    node.next = route;
    return route;
  }
}
