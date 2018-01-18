package apps.pac4j;

import org.jooby.Jooby;
import org.jooby.pac4j.Pac4j;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.profile.facebook.FacebookProfile;

import java.util.List;

public class Pac4jFacebookDemo extends Jooby {

  public static class CustomAuthorizer extends ProfileAuthorizer<FacebookProfile> {

    @Override public boolean isAuthorized(WebContext context, List<FacebookProfile> profiles)
        throws HttpAction {
      return isAnyAuthorized(context, profiles);
    }

    @Override protected boolean isProfileAuthorized(WebContext context, FacebookProfile profile)
        throws HttpAction {
      if (profile == null) {
        return false;
      }
      return profile.getUsername().startsWith("jle");
    }
  }

  {
    use(new Pac4j().client("*", conf -> {
      final FacebookClient client = new FacebookClient("145278422258960",
          "be21409ba8f39b5dae2a7de525484da8");
      return client;
    }, new CustomAuthorizer()));

    get("/", () -> "OK");

    get("/form", () -> "Form");

    get("/user", req -> {
      FacebookProfile profile = require(FacebookProfile.class);
      return profile;
    });
  }

  public static void main(String[] args) throws Exception {
    run(Pac4jFacebookDemo::new, args);
  }
}
