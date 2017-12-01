package io.jooby.router;

import static com.google.common.collect.ImmutableMap.of;
import io.jooby.Route;
import io.jooby.internal.PathPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class PathPatternTest {

  @Test
  public void webStyle() {
    assertTrue(matches("/", "/"));

    assertTrue(matches("/favicon.ico", "/favicon.ico"));

    assertFalse(matches("/pages/*", "/pages"));
    assertTrue(matches("/pages/*", "/pages/"));
    assertTrue(matches("/pages/*", "/pages/yes"));

    assertTrue(matches("/article", "/article"));
    assertTrue(matches("/article/", "/article"));
    assertTrue(matches("/article/near", "/article/near"));
    assertTrue(matches("/article/{id}", "/article/neard", of("id", "neard")));
    assertTrue(matches("/article/{id}", "/article/123", of("id", "123")));
    assertTrue(matches("/article/@{id}", "/article/@peter", of("id", "peter")));
    assertTrue(matches("/article/{id}/{opts}", "/article/123/456", of("id", "123", "opts", "456")));

    assertTrue(matches("/article/{iffd}/edit", "/article/111/edit", of("iffd", "111")));
    assertTrue(matches("/article/{id}/related", "/article/22/related", of("id", "22")));
    assertTrue(matches("/article/slug/{month}/-/{day}/{year}", "/article/slug/sept/-/4/2015",
        of("month", "sept", "year", "2015", "day", "4")));

    assertTrue(matches("/admin/user", "/admin/user"));
    assertTrue(matches("/admin/user/", "/admin/user"));
    assertTrue(matches("/admin/user/{id}", "/admin/user/1", of("id", "1")));
    assertTrue(matches("/admin/apps/{id}", "/admin/apps/33", of("id", "33")));
    assertTrue(matches("/admin/**ff", "/admin/lots/of/:fun", of("ff", "lots/of/:fun")));
    assertTrue(
        matches("/admin/apps/{id}/**ff", "/admin/apps/333/woot", of("ff", "woot", "id", "333")));
    assertTrue(matches("/admin/apps/{id}", "/admin/apps/333", of("id", "333")));

    assertTrue(matches("/hubs/{hubID}/view", "/hubs/123/view", of("hubID", "123")));
    assertTrue(matches("/hubs/{hubID}/view/*", "/hubs/123/view/index.html", of("hubID", "123")));
    assertTrue(matches("/hubs/{hubID}/view/**page", "/hubs/123/view/index.html",
        of("hubID", "123", "page", "index.html")));
    assertTrue(matches("/hubs/{hubID}/users", "/hubs/123/users", of("hubID", "123")));

    assertTrue(matches("/users/super/**f", "/users/super/123/okay/yes", of("f", "123/okay/yes")));
    assertTrue(matches("/users/**f", "/users/123/okay/yes", of("f", "123/okay/yes")));
  }

  @Test
  public void colonID() {
    assertTrue(matches("/article/:id", "/article/neard", of("id", "neard")));
    assertTrue(matches("/article/:id", "/article/123", of("id", "123")));
    assertTrue(matches("/article/@:id", "/article/@peter", of("id", "peter")));
    assertTrue(matches("/article/:id/:opts", "/article/123/456", of("id", "123", "opts", "456")));

    assertTrue(matches("/article/:iffd/edit", "/article/111/edit", of("iffd", "111")));
    assertTrue(matches("/article/:id/related", "/article/22/related", of("id", "22")));
    assertTrue(matches("/article/slug/:month/-/:day/:year", "/article/slug/sept/-/4/2015",
        of("month", "sept", "year", "2015", "day", "4")));

    assertTrue(matches("/admin/user/{id}", "/admin/user/1", of("id", "1")));
    assertTrue(matches("/admin/apps/{id}", "/admin/apps/33", of("id", "33")));
    assertTrue(
        matches("/admin/apps/:id/**ff", "/admin/apps/333/woot", of("ff", "woot", "id", "333")));
    assertTrue(matches("/admin/apps/:id", "/admin/apps/333", of("id", "333")));

    assertTrue(matches("/hubs/:hubID/view", "/hubs/123/view", of("hubID", "123")));
    assertTrue(matches("/hubs/:hubID/view/*", "/hubs/123/view/index.html", of("hubID", "123")));
    assertTrue(matches("/hubs/:hubID/view/**page", "/hubs/123/view/index.html",
        of("hubID", "123", "page", "index.html")));
    assertTrue(matches("/hubs/:hubID/users", "/hubs/123/users", of("hubID", "123")));
  }

  @Test
  public void wildOne() {
    assertTrue(matches("/com/t?st.jsp", "/com/test.jsp"));
    assertTrue(matches("/com/t?st.jsp", "/com/tsst.jsp"));
    assertTrue(matches("/com/t?st.jsp", "/com/tast.jsp"));
    assertTrue(matches("/com/t?st.jsp", "/com/txst.jsp"));
    assertFalse(matches("/com/t?st.jsp", "/com/test1.jsp"));
  }

  @Test
  public void wildMany() {
    assertTrue(matches("/profile/*/edit", "/profile/ee-00-9-k/edit"));
    assertFalse(matches("/profile/*/edit", "/profile/ee-00-9-k/p/edit"));

    assertTrue(matches("/profile/*/*/edit", "/profile/ee-00-9-k/p/edit"));
    assertFalse(matches("/profile/*/*/edit", "/profile/ee-00-9-k/edit"));
    assertFalse(matches("/profile/*/*/edit", "/profile/ee-00-9-k/p/k/edit"));
  }

  @Test
  public void subdir() {
    assertTrue(matches("/com/**/test.jsp", "/com/test.jsp"));
    assertTrue(matches("/com/**/test.jsp", "/com/a/test.jsp"));
    assertFalse(matches("/com/**/test.jsp", "/com/a/testx.jsp"));
    assertFalse(matches("/com/**/test.jsp", "/org/test.jsp"));

    assertTrue(matches("/com/**", "/com/test.jsp"));
    assertTrue(matches("/com/**", "/com/a/test.jsp"));
    assertTrue(matches("/com/**", "/com/a/testx.jsp"));
    assertFalse(matches("/com/**", "/org/test.jsp"));
  }

  @Test
  public void any() {
    assertTrue(matches("*", "/com/test.jsp"));
    assertTrue(matches("/com/**", "/com/test.jsp"));
    assertTrue(matches("/com/**", "/com/a/test.jsp"));
    assertTrue(matches("/com/**", "/com/a/testx.jsp"));
    assertFalse(matches("/com/**", "/org/a/testx.jsp"));
  }

  @Test
  public void any2() {
    assertTrue(matches("/org/**/servlet/*.html", "/org/jooby/a/servlet/test.html"));
    assertTrue(matches("/org/**/servlet/*.html", "/org/jooby/a/b/c/servlet/test.html"));
    assertFalse(matches("/org/**/servlet/*.html", "/org/jooby/a/b/c/servlet/test.js"));
  }

  @Test
  public void anyNamed() {
    assertTrue(matches("/**rest", "/com/test.jsp", of("rest", "com/test.jsp")));
    assertTrue(matches("/com/**rest", "/com/test.jsp", of("rest", "test.jsp")));
    assertTrue(matches("/com/**rest", "/com/a/test.jsp", of("rest", "a/test.jsp")));
    assertTrue(matches("/com/**rest", "/com"));
    assertFalse(matches("/com/**rest", "/test.jsp"));
  }

  @Test
  public void anyNamedInner() {
    assertTrue(matches("/**rest/bar", "/foo/bar", of("rest", "foo")));
    assertTrue(matches("/**rest/bar", "/foo/fuz/bar", of("rest", "foo/fuz")));
  }

  @Test
  public void anyNamedMulti() {
    assertTrue(matches("/com/**first/bar/**second", "/com/foo/bar/moo", of("first", "foo", "second", "moo")));
    assertTrue(matches("/com/**first/bar/**second", "/com/a/foo/bar/moo/baz", of("first", "a/foo", "second", "moo/baz")));
    assertFalse(matches("/com/**first/bar/**second", "/com/foo/baz"));
  }

  @Test
  public void rootVar() {
    assertTrue(matches("/{id}/list", "/xqi/list", of("id", "xqi")));
    assertTrue(matches("/{id}/list", "/123/list", of("id", "123")));
    assertFalse(matches("/{id}/list", "/123/listx"));

    assertTrue(matches("/:id/list", "/xqi/list", of("id", "xqi")));
    assertTrue(matches("/:id/list", "/123/list", of("id", "123")));
    assertFalse(matches("/:id/list", "/123/listx"));
  }

  @Test
  public void varPrefix() {
    assertTrue(matches("/p{id}", "/pxqi", of("id", "xqi")));
    assertTrue(matches("/p:id", "/p123", of("id", "123")));
    assertFalse(matches("/p:id", "/123"));
  }

  @Test
  public void varSuffix() {
    assertTrue(matches("/f{w}o", "/foo", of("w", "o")));
    assertTrue(matches("/f{w}o", "/fooooo", of("w", "oooo")));
    assertTrue(matches("/f{w}o", "/furio", of("w", "uri")));
    assertFalse(matches("/f{w}o", "/fo"));

    assertTrue(matches("/f{w}o/bar", "/foo/bar", of("w", "o")));
  }

  @Test
  public void regex() {
    assertTrue(matches("/user/{id:\\d+}", "/user/123", of("id", "123")));
    assertFalse(matches("/user/{id:\\d+}", "/user/123x"));
    assertFalse(matches("/user/{id:\\d+}", "/user/foo"));
  }

  @Test
  public void antExamples() {
    assertTrue(matches("/*.java", "/.java"));
    assertTrue(matches("/*.java", "/x.java"));
    assertTrue(matches("/*.java", "/FooBar.java"));
    assertFalse(matches("/*.java", "/FooBar.xml"));

    assertTrue(matches("/?.java", "/x.java"));
    assertTrue(matches("/?.java", "/A.java"));
    assertFalse(matches("/?.java", "/.java"));
    assertFalse(matches("/?.java", "/zyz.java"));

    assertTrue(matches("/**/CVS/*", "/CVS/Repository"));
    assertTrue(matches("/**/CVS/*", "/org/apache/CVS/Entries"));
    assertTrue(matches("/**/CVS/*", "/org/apache/jakarta/tools/ant/CVS/Entries"));
    assertFalse(matches("/**/CVS/*", "/org/apache/CVS/foo/bar/Entries"));

    assertTrue(matches("/org/apache/jakarta/**", "/org/apache/jakarta/tools/ant/docs/index.html"));
    assertTrue(matches("/org/apache/jakarta/**", "/org/apache/jakarta/test.xml"));
    assertFalse(matches("/org/apache/jakarta/**", "/org/apache/xyz.java"));

    assertTrue(matches("/org/apache/**/CVS/*", "/org/apache/CVS/Entries"));
    assertTrue(matches("/org/apache/**/CVS/*", "/org/apache/jakarta/tools/ant/CVS/Entries"));
    assertFalse(matches("/org/apache/**/CVS/*", "/org/apache/CVS/foo/bar/Entries"));
  }

  @Test
  public void cornerCase() {
    assertTrue(matches("/search/**", "/search"));
    assertTrue(matches("/m/**", "/merge/login"));
  }

  @Test
  public void ignoreCase() {
    assertTrue(matches("/path/:v", "/PatH/fOo", true, of("v", "fOo")));
    assertTrue(matches("/path/:v", "/path/foO", true, of("v", "foO")));
  }

  @Test
  public void normalizePath() {
    assertEquals("/", Route.normalize("/"));
    assertEquals("/**", Route.normalize("*"));
    assertEquals("/", Route.normalize("//"));
    assertEquals("/foo", Route.normalize("/foo"));
    assertEquals("/foo", Route.normalize("foo"));
    assertEquals("/foo", Route.normalize("//foo"));
    assertEquals("/foo", Route.normalize("/foo/"));
    assertEquals("/foo/bar", Route.normalize("/foo/bar"));
    assertEquals("/foo/bar", Route.normalize("/foo//bar"));
    assertEquals("/foo/bar", Route.normalize("/foo////bar"));
  }

  @Test
  public void antStyle() {
    // test matching with **'s
    assertTrue(matches("/bla/**/bla", "/bla/bla"));
    assertTrue(matches("/**", "/testing/testing"));
    assertTrue(matches("/*/**", "/testing/testing"));
    assertTrue(matches("/**/*", "/testing/testing"));
    assertTrue(matches("/bla/**/bla", "/bla/testing/testing/bla"));
    assertTrue(matches("/bla/**/bla", "/bla/testing/testing/bla/bla"));
    assertTrue(matches("/**/test", "/bla/bla/test"));
    assertTrue(matches("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"));
    assertTrue(matches("/bla*bla/test", "/blaXXXbla/test"));
    assertTrue(matches("/*bla/test", "/XXXbla/test"));
    assertFalse(matches("/bla*bla/test", "/blaXXXbl/test"));
    assertFalse(matches("/*bla/test", "XXXblab/test"));
    assertFalse(matches("/*bla/test", "XXXbl/test"));

    assertFalse(matches("/????", "/bala/bla"));
    assertFalse(matches("/**/*bla", "/bla/bla/bla/bbb"));

    assertTrue(matches("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"));
    assertTrue(matches("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"));
    assertTrue(matches("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"));
    assertTrue(matches("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"));

    assertTrue(matches("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"));
    assertTrue(matches("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"));
    assertTrue(matches("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"));
    assertFalse(matches("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing/testing"));

    assertFalse(matches("/x/x/**/bla", "/x/x/x/"));

    assertTrue(matches("/foo/bar/**", "/foo/bar"));
    assertTrue(matches("/foo/bar/**", "/foo/bar/baz"));

    assertTrue(matches("/foo/bar/**", "/foo/bar"));
    assertTrue(matches("/resource/1", "/resource/1"));
    assertTrue(matches("/resource/*", "/resource/1"));
    assertTrue(matches("/resource/*/", "/resource/1"));
    assertTrue(matches("/top-resource/*/resource/*/sub-resource/*",
        "/top-resource/1/resource/2/sub-resource/3"));
    assertTrue(matches("/top-resource/*/resource/*/sub-resource/*",
        "/top-resource/999999/resource/8888888/sub-resource/77777777"));
    assertTrue(matches("/*/*/*/*/secret.html", "/this/is/protected/path/secret.html"));
    assertTrue(matches("/*/*/*/*/*.html", "/this/is/protected/path/secret.html"));
    assertTrue(matches("/*/*/*/*", "/this/is/protected/path"));
    assertTrue(matches("/org/springframework/**/*.jsp", "/org/springframework/web/views/hello.jsp"));
    assertTrue(matches("/org/springframework/**/*.jsp", "/org/springframework/web/default.jsp"));
    assertTrue(matches("/org/springframework/**/*.jsp", "/org/springframework/default.jsp"));
    assertTrue(matches("/org/**/servlet/bla.jsp", "/org/springframework/servlet/bla.jsp"));
    assertTrue(matches("/org/**/servlet/bla.jsp", "/org/springframework/testing/servlet/bla.jsp"));
    assertTrue(matches("/org/**/servlet/bla.jsp", "/org/servlet/bla.jsp"));
    assertTrue(matches("/**/hello.jsp", "/org/springframework/servlet/web/views/hello.jsp"));
    assertTrue(matches("/**/**/hello.jsp", "/org/springframework/servlet/web/views/hello.jsp"));

    assertFalse(matches("/foo/bar/**", "/foo /bar"));
    assertFalse(matches("/foo/bar/**", "/foo          /bar"));
    assertFalse(matches("/foo/bar/**", "/foo          /               bar"));
    assertFalse(matches("/foo/bar/**", "       /      foo          /               bar"));
    assertFalse(matches("org/**/servlet/bla.jsp", "   org   /      servlet    /   bla   .   jsp"));

    // test matching with ?'s and /'s
    assertTrue(matches("/?", "/a"));
    assertTrue(matches("/?/a", "/a/a"));
    assertTrue(matches("/a/?", "/a/b"));
    assertTrue(matches("/??/a", "/aa/a"));
    assertTrue(matches("/a/??", "/a/bb"));
    assertTrue(matches("/?", "/a"));

    // test matching with *'s
    assertTrue(matches("*", "/test"));
    assertTrue(matches("/test*", "/test"));
    assertTrue(matches("/test*", "/testTest"));
    assertTrue(matches("/test/*", "/test/Test"));
    assertTrue(matches("/test/*", "/test/t"));
    assertTrue(matches("/test/*", "/test/"));
    assertTrue(matches("/*test*", "/AnothertestTest"));
    assertTrue(matches("/*test", "/Anothertest"));
    assertTrue(matches("/*.*", "/test."));
    assertTrue(matches("/*.*", "/test.test"));
    assertTrue(matches("/*.*", "/test.test.test"));
    assertTrue(matches("/test*aaa", "/testblaaaa"));
    assertFalse(matches("/test*", "/tst"));
    assertFalse(matches("/test*", "/tsttest"));
    assertFalse(matches("/test*", "/test/"));
    assertFalse(matches("/test*", "/test/t"));
    assertFalse(matches("/test/*", "/test"));
    assertFalse(matches("v*test*", "/tsttst"));
    assertFalse(matches("/*test", "/tsttst"));
    assertFalse(matches("/*.*", "/tsttst"));
    assertFalse(matches("/test*aaa", "/test"));
    assertFalse(matches("/test*aaa", "/testblaaab"));

    // test exact match
    assertTrue(matches("/test", "/test"));
    assertTrue(matches("/test", "/test"));
    assertFalse(matches("/test.jpg", "test.jpg"));
    assertFalse(matches("/test", "test"));
    assertFalse(matches("/test", "test"));

    // test matching with ?'s
    assertTrue(matches("/t?st", "/test"));
    assertTrue(matches("/??st", "/test"));
    assertTrue(matches("/tes?", "/test"));
    assertTrue(matches("/te??", "/test"));
    assertTrue(matches("/?es?", "/test"));
    assertFalse(matches("/tes?", "/tes"));
    assertFalse(matches("/tes?", "/testt"));
    assertFalse(matches("/tes?", "/tsst"));
  }

  private boolean matches(String pattern, String path) {
    return matches(pattern, path, false, Collections.emptyMap());
  }

  private boolean matches(String pattern, String path, boolean icase) {
    return matches(pattern, path, icase, Collections.emptyMap());
  }

  private boolean matches(String pattern, String path, Map<String, String> vars) {
    return matches(pattern, path, false, vars);
  }

  private boolean matches(String pattern, String path, boolean icase, Map<String, String> vars) {
    PathPattern p = new PathPattern(pattern, icase);
    PathPattern.Result result = p.test(path);
    assertEquals(vars, result.variables);
    return result.matches;
  }
}
