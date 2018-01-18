package org.jooby.internal.pac4j;

import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Route;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;

public class Pac4jLogout implements Route.Handler {

  private final Config conf;
  private final String defaultUrl;
  private final String logoutUrlPattern;
  private final boolean localLogout;
  private final boolean destroySession;
  private final boolean centralLogout;

  public Pac4jLogout(Config conf, String defaultUrl, String logoutUrlPattern, boolean localLogout,
      boolean destroySession, boolean centralLogout) {
    this.conf = conf;
    this.defaultUrl = defaultUrl;
    this.logoutUrlPattern = logoutUrlPattern;
    this.localLogout = localLogout;
    this.destroySession = destroySession;
    this.centralLogout = centralLogout;
  }

  @Override public void handle(Request req, Response rsp) throws Throwable {
    WebContext context = req.require(WebContext.class);
    String redirectTo = req.<String>ifGet("pac4j.logout.redirectTo").orElse(defaultUrl);
    conf.getLogoutLogic()
        .perform(context, conf, conf.getHttpActionAdapter(), redirectTo, logoutUrlPattern,
            localLogout, destroySession, centralLogout);
  }
}
