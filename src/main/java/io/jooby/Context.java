package io.jooby;

import org.jooby.funzy.Throwing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public interface Context {

  class Dispatched extends RuntimeException {
    public final Executor executor;

    public Dispatched(@Nullable Executor executor) {
      this.executor = executor;
    }
  }

  /**
   * **********************************************************************************************
   * **** Request methods *************************************************************************
   * **********************************************************************************************
   */
  @Nonnull QueryString query();

  @Nonnull Value query(String name);

  @Nonnull String method();

  @Nonnull String path();

  @Nonnull Route route();

  @Nonnull Context route(@Nonnull Route route);

  boolean isInIoThread();

  default @Nonnull Context dispatch() {
    return dispatch(null);
  }

  @Nonnull Context dispatch(@Nullable Executor executor);

  @Nonnull Context detach();

  /**
   * **********************************************************************************************
   * **** Response methods *************************************************************************
   * **********************************************************************************************
   */

  @Nonnull Context reset();

  @Nonnull OutputStream outputStream();

  default @Nonnull Context outputStream(@Nonnull Throwing.Consumer<OutputStream> callback) {
    try (OutputStream w = outputStream()) {
      callback.accept(w);
    } catch (Throwable x) {
      throw Throwing.sneakyThrow(x);
    } finally {
      end();
    }
    return this;
  }

  default @Nonnull Writer writer() {
    return writer(StandardCharsets.UTF_8);
  }

  default @Nonnull Context writer(@Nonnull Throwing.Consumer<Writer> writer) {
    return writer(StandardCharsets.UTF_8, writer);
  }

  default @Nonnull Context writer(@Nonnull Charset charset, @Nonnull Throwing.Consumer<Writer> writer) {
    try (Writer w = writer(charset)) {
      writer.accept(w);
    } catch (Throwable x) {
      throw Throwing.sneakyThrow(x);
    } finally {
      end();
    }
    return this;
  }

  default @Nonnull Writer writer(@Nonnull Charset charset) {
    return new OutputStreamWriter(outputStream(), charset);
  }

  @Nonnull Context after(@Nonnull Route.After after);

  default @Nonnull Context header(@Nonnull String name, int value) {
    return header(name, Integer.toString(value));
  }

  @Nonnull Context header(@Nonnull String name, @Nonnull String value);

  int status();

  @Nonnull Context status(int status);

  @Nonnull Context length(long length);

  @Nonnull Context type(@Nonnull String contentType);

  default @Nonnull Context write(@Nonnull String chunk) {
    return write(chunk, StandardCharsets.UTF_8);
  }

  @Nonnull Context write(@Nonnull String chunk, @Nonnull Charset charset);

  @Nonnull Context write(byte[] chunk);

  @Nonnull Context write(@Nonnull ByteBuffer chunk);

  default @Nonnull Context send(@Nonnull String response) {
    return send(response, StandardCharsets.UTF_8);
  }

  @Nonnull Context send(@Nonnull String response, @Nonnull Charset charset);

  @Nonnull Context send(@Nonnull byte[] chunk);

  @Nonnull Context send(@Nonnull ByteBuffer chunk);

  @Nonnull Context end();

  boolean committed();
}
