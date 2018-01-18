package org.jooby.pac4j;

import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.Route;
import org.jooby.Router;
import org.jooby.Session;
import org.jooby.internal.pac4j.ClientType;
import org.jooby.internal.pac4j.FormFilter;
import org.jooby.internal.pac4j.Pac4jActionAdapter;
import org.jooby.internal.pac4j.Pac4jCallback;
import org.jooby.internal.pac4j.Pac4jContext;
import org.jooby.internal.pac4j.Pac4jSecurityFilter;
import org.jooby.internal.pac4j.Pac4jLogout;
import org.jooby.internal.pac4j.Pac4jSessionStore;
import org.jooby.internal.pac4j.ProfileManagerProvider;
import org.jooby.scope.Providers;
import org.jooby.scope.RequestScoped;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;

import javax.inject.Provider;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Pac4j implements Jooby.Module {

  private static class ClientConfig {
    private String pattern;

    private String authorizer;

    private Client client;

    public ClientConfig(String pattern, String authorizer, Client client) {
      this.pattern = pattern;
      this.authorizer = authorizer;
      this.client = client;
    }
  }

  public static final TypeLiteral<SessionStore<WebContext>> SSTORE = new TypeLiteral<SessionStore<WebContext>>() {
  };

  private final org.pac4j.core.config.Config config = new org.pac4j.core.config.Config();

  private final List<BiFunction<Config, Binder, ClientConfig>> clients = new ArrayList<>();

  private boolean showSimpleForm;

  public <C extends Credentials, U extends CommonProfile> Pac4j client(
      Function<Config, Client<C, U>> clientProvider) {
    return client("*", clientProvider, null);
  }

  public <C extends Credentials, U extends CommonProfile> Pac4j client(String pattern,
      Function<Config, Client<C, U>> clientProvider) {
    return client(pattern, clientProvider, null);
  }

  public <C extends Credentials, U extends CommonProfile> Pac4j client(String pattern,
      Function<Config, Client<C, U>> clientProvider, Authorizer<U> authorizer) {
    clients.add((conf, binder) -> {
      Client<C, U> client = clientProvider.apply(conf);
      String authorizerName = null;
      if (authorizer != null) {
        authorizerName = authorizer.getClass().getSimpleName();
        config.addAuthorizer(authorizerName, authorizer);
      }
      ClientType.profileTypes(ClientType.clientType(client.getClass()), profile ->
          binder
              .bind(profile)
              .toProvider(Providers.outOfScope(profile))
              .in(RequestScoped.class)
      );

      return new ClientConfig(pattern, authorizerName, client);
    });
    return this;
  }

  public Pac4j form() {
    return form("*");
  }

  public Pac4j form(String pattern) {
    return client(pattern, conf -> {
      showSimpleForm = true;
      return new FormClient("/login", new SimpleTestUsernamePasswordAuthenticator());
    }, null);
  }

  @Override public void configure(Env env, Config conf, Binder binder) throws Throwable {
    String contextPath = conf.getString("application.path");
    String callbackPath = conf.getString("pac4j.callback.path");

    /** Pac4j Clients: */
    List<Client> clientList = new ArrayList<>();
    List<ClientConfig> securityRoutes = this.clients.stream()
        .map(fn -> fn.apply(conf, binder))
        .collect(Collectors.toList());
    securityRoutes.forEach(it -> clientList.add(it.client));

    if (clientList.size() == 0) {
      // default form
      form();
    }

    Clients clients = new Clients(
        URI.create(conf.getString("pac4j.callback.url")).normalize().toString(),
        clientList);
    config.setClients(clients);
    config.setHttpActionAdapter(new Pac4jActionAdapter());

    boolean multiProfile = clientList.size() > 1;
    Router router = env.router();

    /** Config: */
    binder.bind(org.pac4j.core.config.Config.class).toInstance(config);

    /** WebContext: */
    binder.bind(WebContext.class).to(Pac4jContext.class);

    /** Profile manager: */
    Function<WebContext, ProfileManager> pmf = config.getProfileManagerFactory();
    if (pmf == null) {
      pmf = ProfileManager::new;
      config.setProfileManagerFactory(pmf);
    }
    binder.bind(ProfileManager.class).toProvider(ProfileManagerProvider.class);

    /** Session store: */
    SessionStore<WebContext> sessionStore = config.getSessionStore();
    if (sessionStore == null) {
      binder.bind(SSTORE).to(Pac4jSessionStore.class);
    } else {
      binder.bind(SSTORE).toInstance(sessionStore);
    }

    /** DEV Login form: */
    if (showSimpleForm) {
      router.get("/login",
          new FormFilter(contextPath + callbackPath))
          .name("pac4j(LoginForm)");
    }

    /** Callback: */
    CallbackLogic callbackLogic = config.getCallbackLogic();
    if (callbackLogic == null) {
      callbackLogic = new DefaultCallbackLogic();
      config.setCallbackLogic(callbackLogic);
    }
    boolean renewSession = conf.getBoolean("pac4j.callback.renewSession");
    List<String> excludePaths = conf.getStringList("pac4j.excludePaths");
    router.use(conf.getString("pac4j.callback.method"), callbackPath,
        new Pac4jCallback(config, contextPath, multiProfile, renewSession))
        .excludes(excludePaths)
        .name("pac4j(Callback)");

    /** Security Filters: */
    SecurityLogic securityLogic = config.getSecurityLogic();
    if (securityLogic == null) {
      securityLogic = new DefaultSecurityLogic();
      config.setSecurityLogic(securityLogic);
    }
    Map<String, Pac4jSecurityFilter> filters = new LinkedHashMap<>();
    securityRoutes.forEach(it -> {
      String pattern = Route.normalize(it.pattern);
      Pac4jSecurityFilter filter = filters.get(pattern);
      String clientName = it.client.getName();
      if (filter == null) {
        filter = new Pac4jSecurityFilter(config, clientName, it.authorizer, null, multiProfile);
        filters.put(pattern, filter);
      } else {
        // multiple clients per pattern
        filter.addClient(clientName);
      }
    });

    filters.forEach((pattern, filter) -> {
      router.use(conf.getString("pac4j.securityFilter.method"), pattern, filter)
          .excludes(excludePaths)
          .name("pac4j(" + filter + ")");
    });

    /** Logout: */
    LogoutLogic logoutLogic = config.getLogoutLogic();
    if (logoutLogic == null) {
      logoutLogic = new DefaultLogoutLogic<>();
      config.setLogoutLogic(logoutLogic);
    }
    router.use(conf.getString("pac4j.logout.method"), conf.getString("pac4j.logout.path"),
        new Pac4jLogout(config,
            conf.getString("pac4j.logout.redirectTo"),
            conf.getString("pac4j.logout.pattern"),
            conf.getBoolean("pac4j.logout.destroySession"),
            conf.getBoolean("pac4j.logout.local"),
            conf.getBoolean("pac4j.logout.central")))
        .name("pac4j(Logout)");
  }

  @Override public Config config() {
    return ConfigFactory.parseResources(getClass(), "pac4j.conf");
  }

}
