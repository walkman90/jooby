package org.jooby.internal.pac4j;

import com.google.common.collect.ImmutableMap;
import org.jooby.Err;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Status;
import org.jooby.funzy.Throwing;
import org.jooby.funzy.Try;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Pac4jContext implements WebContext {

  private static final String[] NO_PARAM = {null};

  private final Map<String, String[]> params;
  private final Request req;
  private final Response rsp;
  private SessionStore sessionStore;

  @Inject
  public Pac4jContext(Request req, Response rsp, SessionStore<WebContext> sessionStore) {
    this.req = req;
    this.rsp = rsp;
    this.params = params(req);
    this.sessionStore = sessionStore;
  }

  @Override public SessionStore getSessionStore() {
    return sessionStore;
  }

  @Override public void setSessionStore(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
  }

  @Override public String getRequestParameter(String name) {
    return params.getOrDefault(name, NO_PARAM)[0];
  }

  @Override public Map<String, String[]> getRequestParameters() {
    return params;
  }

  @Override public Object getRequestAttribute(String name) {
    return req.ifGet(name).orElse(null);
  }

  @Override public void setRequestAttribute(String name, Object value) {
    req.set(name, value);
  }

  @Override public String getRequestHeader(String name) {
    return req.header(name).toOptional().orElse(null);
  }

  @Override public void setSessionAttribute(String name, Object value) {
    sessionStore.set(this, name, value);
  }

  @Override public Object getSessionAttribute(String name) {
    return sessionStore.get(this, name);
  }

  @Override public String getSessionIdentifier() {
    return sessionStore.getOrCreateSessionId(this);
  }

  @Override public String getRequestMethod() {
    return req.method();
  }

  @Override public String getRemoteAddr() {
    return req.ip();
  }

  @Override public void writeResponseContent(String content) {
    Try.run(() -> rsp.send(content));
  }

  @Override public void setResponseStatus(int code) {
    rsp.status(code);
  }

  @Override public void setResponseHeader(String name, String value) {
    rsp.header(name, value);
  }

  @Override public void setResponseContentType(String content) {
    rsp.type(content);
  }

  @Override public String getServerName() {
    return req.hostname();
  }

  @Override public int getServerPort() {
    return req.port();
  }

  @Override public String getScheme() {
    return req.secure() ? "https" : "http";
  }

  @Override public boolean isSecure() {
    return req.secure();
  }

  @Override public String getFullRequestURL() {
    String query = req.queryString().map(it -> "?" + it).orElse("");
    return getScheme() + "://" + getServerName() + ":" + getServerPort() + req.contextPath() + req
        .path() + query;
  }

  @Override public Collection<Cookie> getRequestCookies() {
    return req.cookies().stream().map(c -> {
      Cookie cookie = new Cookie(c.name(), c.value().orElse(null));
      c.domain().ifPresent(cookie::setDomain);
      c.path().ifPresent(cookie::setPath);
      cookie.setSecure(c.secure());
      cookie.setHttpOnly(c.httpOnly());
      return cookie;
    }).collect(Collectors.toList());
  }

  @Override public void addResponseCookie(Cookie cookie) {
    org.jooby.Cookie.Definition c = new org.jooby.Cookie.Definition(cookie.getName(),
        cookie.getValue());
    Optional.ofNullable(cookie.getDomain()).ifPresent(c::domain);
    Optional.ofNullable(cookie.getPath()).ifPresent(c::path);
    c.httpOnly(cookie.isHttpOnly());
    c.maxAge(cookie.getMaxAge());
    c.secure(cookie.isSecure());
    rsp.cookie(c);
  }

  @Override public String getPath() {
    return req.path();
  }

  @Override public String getRequestContent() {
    return Try.apply(() -> req.body().value()).get();
  }

  private Map<String, String[]> params(final Request req) {
    ImmutableMap.Builder<String, String[]> result = ImmutableMap.<String, String[]>builder();

    req.params().toMap().forEach((name, value) -> {
      try {
        List<String> values = value.toList();
        result.put(name, values.toArray(new String[values.size()]));
      } catch (Err ignored) {
        LoggerFactory.getLogger(getClass()).debug("ignoring HTTP param: " + name, ignored);
      }
    });
    return result.build();
  }
}
