package org.jooby.internal.pac4j;

import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Route;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;

public class Pac4jCallback implements Route.Handler {
  private final Config pac4j;
  private final String defaultUrl;
  private final boolean multiProfile;
  private final boolean renewSession;

  public Pac4jCallback(Config pac4j, String defaultUrl, boolean multiProfile, boolean renewSession) {
    this.pac4j = pac4j;
    this.defaultUrl = defaultUrl;
    this.multiProfile = multiProfile;
    this.renewSession = renewSession;
  }

  @Override public void handle(Request req, Response rsp) throws Throwable {
    WebContext context = req.require(WebContext.class);
    pac4j.getCallbackLogic()
        .perform(context, pac4j, pac4j.getHttpActionAdapter(), defaultUrl, multiProfile,
            renewSession);
  }
}
