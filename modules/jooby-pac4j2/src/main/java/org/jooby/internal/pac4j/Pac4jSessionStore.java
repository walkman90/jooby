package org.jooby.internal.pac4j;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Primitives;
import org.jooby.Request;
import org.jooby.Session;
import org.jooby.funzy.Try;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Pac4jSessionStore implements SessionStore<WebContext> {
  private static final String PREFIX = "b64~";

  private final Request req;

  @Inject
  public Pac4jSessionStore(Request req) {
    this.req = req;
  }

  @Override public String getOrCreateSessionId(WebContext context) {
    return req.session().id();
  }

  @Override public Object get(WebContext context, String key) {
    return req.ifSession()
        .map(session -> {
          String value = session.get(key).toOptional().orElse(null);
          return value == null ? null : strToObject(value);
        }).orElse(null);
  }

  @Override public void set(WebContext context, String key, Object value) {
    if (value == null || value.toString().length() == 0) {
      req.session().unset(key);
    } else {
      req.session().set(key, objToStr(value));
    }
  }

  @Override public boolean destroySession(WebContext pac4jContext) {
    req.ifSession().ifPresent(Session::destroy);
    return true;
  }

  @Override public Object getTrackableSession(WebContext pac4jContext) {
    return req;
  }

  @Override
  public SessionStore<WebContext> buildFromTrackableSession(WebContext pac4jContext, Object o) {
    if (o instanceof Request) {
      return new Pac4jSessionStore(req);
    }
    return null;
  }

  @Override public boolean renewSession(WebContext pac4jContext) {
    return req.ifSession()
        .map(session -> {
          final Map<String, String> attributes = new HashMap<>(session.attributes());
          session.destroy();
          final Session newSession = req.session();
          attributes.forEach(newSession::set);
          return true;
        })
        .orElse(false);
  }

  public static final Object strToObject(final String value) {
    if (value == null || !value.startsWith(PREFIX)) {
      return value;
    }
    return Try.apply(() -> {
      byte[] bytes = BaseEncoding.base64().decode(value.substring(PREFIX.length()));
      return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    })
        .wrap(x -> new IllegalArgumentException("Can't de-serialize value " + value, x))
        .get();
  }

  public static final String objToStr(final Object value) {
    if (value instanceof CharSequence || Primitives.isWrapperType(value.getClass())) {
      return value.toString();
    }
    return Try.apply(() -> {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(bytes);
      stream.writeObject(value);
      stream.flush();
      return PREFIX + BaseEncoding.base64().encode(bytes.toByteArray());
    }).get();
  }
}
