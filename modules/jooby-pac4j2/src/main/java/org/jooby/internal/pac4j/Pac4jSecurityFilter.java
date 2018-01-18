package org.jooby.internal.pac4j;

import org.jooby.Err;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Route;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;

public class Pac4jSecurityFilter implements Route.Filter {

  private final Config conf;
  private String clients;
  private final String matchers;
  private final boolean multiProfile;
  private String authorizers;

  public Pac4jSecurityFilter(Config conf, String clients,
      String authorizers, String matchers, boolean multiProfile) {
    this.conf = conf;
    this.clients = clients;
    this.authorizers = authorizers;
    this.matchers = matchers;
    this.multiProfile = multiProfile;
  }

  public void addClient(String client) {
    this.clients += Pac4jConstants.ELEMENT_SEPRATOR + client;
  }

  @Override public void handle(Request req, Response rsp, Route.Chain chain) throws Throwable {
    try {
      WebContext context = req.require(WebContext.class);
      conf.getSecurityLogic().perform(context, conf, (ctx, parameters) -> {
            ProfileManager pm = req.require(ProfileManager.class);
            pm.get(req.ifSession().isPresent()).ifPresent(profile ->
                ClientType.profileTypes(profile.getClass(), type -> req.set(type, profile))
            );
            chain.next(req, rsp);
            return null;
          },
          conf.getHttpActionAdapter(), clients, authorizers, matchers, multiProfile);
    } catch (TechnicalException x) {
      Throwable cause = x.getCause();
      if (!(cause instanceof Err)) {
        // Pac4j wrap everything as TechnicalException, it makes stacktrace too ugly, so we rethrow
        // Err
        cause = x;
      }
      throw cause;
    }
  }

  @Override public String toString() {
    return clients;
  }
}
