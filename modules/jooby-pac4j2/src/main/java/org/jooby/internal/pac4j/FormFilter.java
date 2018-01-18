package org.jooby.internal.pac4j;

import org.jooby.MediaType;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Route;
import org.jooby.Route.Chain;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.indirect.FormClient;

public class FormFilter implements Route.Handler {

  private static final String FORM = "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "<title>Login Page</title>\n" +
      "<script type=\"text/javascript\">\n" +
      "\n" +
      "  function submitForm() {\n" +
      "    document.form.submit();\n" +
      "  }\n" +
      "\n" +
      "  function onKeyPressEvent(event) {\n" +
      "    var key = event.keyCode || event.which;\n" +
      "    if (key === 13) {\n" +
      "      if (event.preventDefault) {\n" +
      "        event.preventDefault();\n" +
      "      } else {\n" +
      "        event.returnValue = false;\n" +
      "      }\n" +
      "\n" +
      "      submitForm('');\n" +
      "      return false;\n" +
      "    }\n" +
      "  }\n" +
      "</script>\n" +
      "</head>\n" +
      "<body onload=\"document.form.username.focus();\">\n" +
      "  <h3>Login</h3>\n" +
      "  <p style=\"color: red;\">\n" +
      "  %s\n" +
      "  </p>\n" +
      "  <form name=\"form\" action=\"%s\" method=\"POST\">\n" +
      "    <input name=\"username\" onkeypress=\"onKeyPressEvent(event)\" value=\"%s\" />\n" +
      "    <p></p>\n" +
      "    <input type=\"password\" name=\"password\" onkeypress=\"onKeyPressEvent(event)\" />\n" +
      "    <p></p>\n" +
      "    <input type=\"submit\" value=\"Submit\" />\n" +
      "  </form>\n" +
      "</body>\n" +
      "</html>\n";

  private String callback;

  public FormFilter(String callback) {
    this.callback = Route.normalize(callback) + "?client_name=FormClient";
  }

  @Override
  public void handle(final Request req, final Response rsp) throws Throwable {
    String error = req.param("error").toOptional().orElse("");
    String username = req.param("username").toOptional().orElse("");

    req.set("username", username);
    req.set("error", error);

    // default login form
    rsp.type(MediaType.html).send(String.format(FORM, error, callback, username));
  }

}
