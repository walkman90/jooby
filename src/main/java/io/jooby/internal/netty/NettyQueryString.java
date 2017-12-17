package io.jooby.internal.netty;

import io.jooby.QueryString;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NettyQueryString implements QueryString {

  private static final List<String> EMPTY_LIST = Collections.emptyList();

  private final QueryStringDecoder decoder;

  public NettyQueryString(String uri) {
    decoder = new QueryStringDecoder(uri, StandardCharsets.UTF_8);
  }

  @Override public String raw() {
    return decoder.rawQuery();
  }

  @Override public Stream<String> names() {
    return decoder.parameters().keySet().stream();
  }

  @Override public List<String> values(String name) {
    return decoder.parameters().getOrDefault(name, EMPTY_LIST);
  }

  @Override public String toString() {
    return raw();
  }
}
