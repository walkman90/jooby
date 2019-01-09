/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Copyright 2014 Edgar Espina
 */
package io.jooby;

import io.jooby.internal.UrlParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public interface Context {

  String ACCEPT = "Accept";

  ZoneId GMT = ZoneId.of("GMT");

  String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

  DateTimeFormatter RFC1123 = DateTimeFormatter
      .ofPattern(RFC1123_PATTERN, Locale.US)
      .withZone(GMT);

  /**
   * **********************************************************************************************
   * **** Native methods *************************************************************************
   * **********************************************************************************************
   */

  @Nonnull Router router();

  /**
   * **********************************************************************************************
   * **** Request methods *************************************************************************
   * **********************************************************************************************
   */
  @Nonnull String method();

  @Nonnull Route route();

  @Nonnull Context route(@Nonnull Route route);

  /**
   * Request path without decoding (a.k.a raw Path). QueryString (if any) is not included.
   *
   * @return Request path without decoding (a.k.a raw Path). QueryString (if any) is not included.
   */
  @Nonnull String pathString();

  @Nonnull default Value path(@Nonnull String name) {
    String value = pathMap().get(name);
    return value == null ?
        new Value.Missing(name) :
        new Value.Simple(name, UrlParser.decodePath(value));
  }

  @Nonnull default Value path() {
    return Value.path(pathMap());
  }

  @Nonnull Map<String, String> pathMap();

  @Nonnull Context pathMap(@Nonnull Map<String, String> pathMap);

  /* **********************************************************************************************
   * Query String API
   * **********************************************************************************************
   */
  @Nonnull QueryString query();

  @Nonnull default Value query(@Nonnull String name) {
    return query().get(name);
  }

  /**
   * Query string with the leading <code>?</code> or empty string.
   *
   * @return Query string with the leading <code>?</code> or empty string.
   */
  @Nonnull default String queryString() {
    return query().queryString();
  }

  @Nonnull default <T> T query(Reified<T> type) {
    return query().to(type);
  }

  @Nonnull default <T> T query(Class<T> type) {
    return query().to(type);
  }

  @Nonnull default Map<String, String> queryMap() {
    return query().toMap();
  }

  @Nonnull default Map<String, List<String>> queryMultimap() {
    return query().toMultimap();
  }

  /* **********************************************************************************************
   * Header API
   * **********************************************************************************************
   */

  @Nonnull Value headers();

  @Nonnull default Value header(@Nonnull String name) {
    return headers().get(name);
  }

  @Nonnull default Map<String, String> headerMap() {
    return headers().toMap();
  }

  @Nonnull default Map<String, List<String>> headerMultimap() {
    return headers().toMultimap();
  }

  default boolean accept(String contentType) {
    String accept = header(ACCEPT).value(MediaType.ALL);
    return MediaType.matches(contentType, accept);
  }

  /* **********************************************************************************************
   * Form API
   * **********************************************************************************************
   */

  @Nonnull Formdata form();

  @Nonnull default Map<String, List<String>> formMultimap() {
    return form().toMultimap();
  }

  @Nonnull default Map<String, String> formMap() {
    return form().toMap();
  }

  @Nonnull default Value form(@Nonnull String name) {
    return form().get(name);
  }

  @Nonnull default <T> T form(Reified<T> type) {
    return form().to(type);
  }

  @Nonnull default <T> T form(Class<T> type) {
    return form().to(type);
  }

  /* **********************************************************************************************
   * Multipart API
   * **********************************************************************************************
   */

  /**
   * Parse a multipart/form-data request and returns the result.
   *
   * <strong>NOTE:</strong> this method throws an {@link IllegalStateException} when call it from
   * <code>EVENT_LOOP thread</code>;
   *
   * @return Multipart node.
   */
  @Nonnull Multipart multipart();

  @Nonnull default Value multipart(@Nonnull String name) {
    return multipart().get(name);
  }

  @Nonnull default <T> T multipart(Reified<T> type) {
    return multipart().to(type);
  }

  @Nonnull default <T> T multipart(Class<T> type) {
    return multipart().to(type);
  }

  @Nonnull default Map<String, List<String>> multipartMultimap() {
    return multipart().toMultimap();
  }

  @Nonnull default Map<String, String> multipartMap() {
    return multipart().toMap();
  }

  @Nonnull default List<FileUpload> files() {
    Value multipart = multipart();
    List<FileUpload> result = new ArrayList<>();
    for (Value value : multipart) {
      if (value.isUpload()) {
        result.add(value.fileUpload());
      }
    }
    return result;
  }

  @Nonnull default List<FileUpload> files(@Nonnull String name) {
    Value multipart = multipart(name);
    List<FileUpload> result = new ArrayList<>();
    for (Value value : multipart) {
      result.add(value.fileUpload());
    }
    return result;
  }

  @Nonnull default FileUpload file(@Nonnull String name) {
    return multipart(name).fileUpload();
  }

  /* **********************************************************************************************
   * Request Body
   * **********************************************************************************************
   */

  @Nonnull Body body();

  default @Nonnull <T> T body(@Nonnull Reified<T> type) {
    return body(type.getType());
  }

  default @Nonnull <T> T body(@Nonnull Reified<T> type, @Nonnull String contentType) {
    return body(type.getType(), contentType);
  }

  default @Nonnull <T> T body(@Nonnull Type type) {
    String contentType = header("Content-Type").value("text/plain");
    int i = contentType.indexOf(';');
    if (i > 0) {
      return body(type, contentType.substring(0, i));
    }
    return body(type, contentType);
  }

  default @Nonnull <T> T body(@Nonnull Type type, @Nonnull String contentType) {
    try {
      return parser(contentType).parse(this, type);
    } catch (Exception x) {
      throw Throwing.sneakyThrow(x);
    }
  }

  /* **********************************************************************************************
   * Body Parser
   * **********************************************************************************************
   */
  default @Nonnull Parser parser(@Nonnull String contentType) {
    return route().parser(contentType);
  }

  /* **********************************************************************************************
   * Dispatch methods
   * **********************************************************************************************
   */
  boolean isInIoThread();

  @Nonnull Context dispatch(@Nonnull Runnable action);

  @Nonnull Context dispatch(@Nonnull Executor executor, @Nonnull Runnable action);

  @Nonnull Context detach(@Nonnull Runnable action);

  @Nullable <T> T get(String name);

  @Nonnull Context set(@Nonnull String name, @Nonnull Object value);

  @Nonnull Map<String, Object> locals();

  /**
   * **********************************************************************************************
   * **** Response methods *************************************************************************
   * **********************************************************************************************
   */

  @Nonnull default Context header(@Nonnull String name, @Nonnull Date value) {
    return header(name, RFC1123.format(Instant.ofEpochMilli(value.getTime())));
  }

  @Nonnull default Context header(@Nonnull String name, @Nonnull Instant value) {
    return header(name, RFC1123.format(value));
  }

  @Nonnull default Context header(@Nonnull String name, @Nonnull Object value) {
    if (value instanceof Date) {
      return header(name, (Date) value);
    }
    if (value instanceof Instant) {
      return header(name, (Instant) value);
    }
    return header(name, value.toString());
  }

  @Nonnull Context header(@Nonnull String name, @Nonnull String value);

  @Nonnull Context length(long length);

  @Nonnull default Context type(@Nonnull MediaType contentType) {
    return type(contentType.value(), contentType.charset());
  }

  @Nonnull default Context type(@Nonnull String contentType) {
    return type(MediaType.valueOf(contentType));
  }

  @Nonnull Context type(@Nonnull String contentType, @Nullable String charset);

  @Nonnull default Context statusCode(StatusCode statusCode) {
    return statusCode(statusCode.value());
  }

  @Nonnull Context statusCode(int statusCode);

  default @Nonnull Context render(@Nonnull Object result) {
    try {
      route().renderer().render(this, result);
      return this;
    } catch (Exception x) {
      throw Throwing.sneakyThrow(x);
    }
  }

  @Nonnull Context responseChannel(Throwing.Consumer<WritableByteChannel> consumer) throws
      Exception;

  default @Nonnull Context outputStream(Throwing.Consumer<OutputStream> consumer) throws Exception {
    return responseChannel(channel -> consumer.accept(Channels.newOutputStream(channel)));
  }

  default @Nonnull Context writer(Throwing.Consumer<Writer> consumer) throws Exception {
    return writer(StandardCharsets.UTF_8, consumer);
  }

  default @Nonnull Context writer(Charset charset, Throwing.Consumer<Writer> consumer)
      throws Exception {
    return responseChannel(channel -> {
      try (Writer writer = Channels.newWriter(channel, charset.newEncoder(), Server._16KB)) {
        consumer.accept(writer);
      }
    });
  }

  default @Nonnull Context sendText(@Nonnull String data) {
    return sendText(data, StandardCharsets.UTF_8);
  }

  @Nonnull Context sendText(@Nonnull String data, @Nonnull Charset charset);

  @Nonnull Context sendBytes(@Nonnull byte[] data);

  @Nonnull Context sendBytes(@Nonnull ByteBuffer data);

  @Nonnull default Context sendStatusCode(StatusCode statusCode) {
    return sendStatusCode(statusCode.value());
  }

  @Nonnull Context sendStatusCode(int statusCode);

  @Nonnull default Context sendError(@Nonnull Throwable cause) {
    sendError(cause, router().errorCode(cause));
    if (Throwing.isFatal(cause)) {
      throw Throwing.sneakyThrow(cause);
    }
    return this;
  }

  @Nonnull default Context sendError(@Nonnull Throwable cause, StatusCode statusCode) {
    router().errorHandler().apply(this, cause, statusCode);
    return this;
  }

  boolean isResponseStarted();

  /**
   * Name of the underlying HTTP server: netty, utow, jetty, etc..
   *
   * @return Name of the underlying HTTP server: netty, utow, jetty, etc..
   */
  String name();
}