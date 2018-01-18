package apps.pac4j;

import org.jooby.Jooby;
import org.jooby.pac4j.Pac4j;
import org.pac4j.core.profile.UserProfile;

import java.util.List;

public class Pac4jFormDemo extends Jooby {

  {
    use(new Pac4j().form());

    get("/", () -> "OK");

    get("/form", () -> "Form");

    get("/user", req -> {
      UserProfile profile = require(UserProfile.class);
      return profile;
    });
  }

  public static void main(String[] args) throws Exception {
    run(Pac4jFormDemo::new, args);
  }
}
