package org.jooby.internal.pac4j;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;

import javax.inject.Inject;
import javax.inject.Provider;

public class ProfileManagerProvider implements Provider<ProfileManager> {
  private final ProfileManager pm;

  @Inject
  public ProfileManagerProvider(Config config, WebContext ctx) {
    this.pm = config.getProfileManagerFactory().apply(ctx);
  }

  @Override public ProfileManager get() {
    return pm;
  }
}
