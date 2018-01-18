package org.jooby.internal.pac4j;

import org.jooby.Err;
import org.jooby.Status;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.HttpActionAdapter;

public class Pac4jActionAdapter implements HttpActionAdapter<Object, WebContext> {

  @Override public Object adapt(int code, WebContext context) {
    Status statusCode = Status.valueOf(code);
    if (statusCode.isError()) {
      throw new Err(statusCode);
    }
    // Any other result is already handled by pac4j via webcontext
    return null;
  }
}
