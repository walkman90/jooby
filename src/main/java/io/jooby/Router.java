package io.jooby;

import java.util.Iterator;

public interface Router {

  String GET = "GET";

  String POST = "POST";

  String PUT = "PUT";

  String DELETE = "DELETE";

  String PATCH = "PATCH";

  default Route before(String method, String pattern, Route.Before handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default Route after(String method, String pattern, Route.After handler) {
    return define(method.toUpperCase(), pattern, handler);
  }

  default Route get(String pattern, Route.Handler handler) {
    return define(GET, pattern, handler);
  }

  default Route post(String pattern, Route.Handler handler) {
    return define(POST, pattern, handler);
  }

  default Route put(String pattern, Route.Handler handler) {
    return define(PUT, pattern, handler);
  }

  default Route delete(String pattern, Route.Handler handler) {
    return define(DELETE, pattern, handler);
  }

  default Route patch(String pattern, Route.Handler handler) {
    return define(PATCH, pattern, handler);
  }

  Iterator<Route> iterator(String method, String path);

  Route define(String method, String pattern, Route.Filter handler);
}
