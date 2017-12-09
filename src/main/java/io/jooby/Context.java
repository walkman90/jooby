package io.jooby;

import org.jooby.funzy.Throwing;

import javax.annotation.Nonnull;
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

    public Dispatched(Executor executor) {
      this.executor = executor;
    }
  }

  class Detached extends RuntimeException {
  }

  /**
   * **********************************************************************************************
   * **** Request methods *************************************************************************
   * **********************************************************************************************
   */
  @Nonnull String path();

  @Nonnull Route route();

  @Nonnull Context route(@Nonnull Route route);

  boolean isInIoThread();

  default Context dispatch() {
    return dispatch(null);
  }

  Context dispatch(Executor executor);

  boolean isDetached();

  Context detach();

  /**
   * **********************************************************************************************
   * **** Response methods *************************************************************************
   * **********************************************************************************************
   */

  OutputStream outputStream();

  default Context outputStream(Throwing.Consumer<OutputStream> callback) {
    try (OutputStream w = outputStream()) {
      callback.accept(w);
    } catch (Throwable x) {
      throw Throwing.sneakyThrow(x);
    } finally {
      end();
    }
    return this;
  }

  default Writer writer() {
    return writer(StandardCharsets.UTF_8);
  }

  default Context writer(Throwing.Consumer<Writer> writer) {
    return writer(StandardCharsets.UTF_8, writer);
  }

  default Context writer(Charset charset, Throwing.Consumer<Writer> writer) {
    try (Writer w = writer(charset)) {
      writer.accept(w);
    } catch (Throwable x) {
      throw Throwing.sneakyThrow(x);
    } finally {
      end();
    }
    return this;
  }

  default Writer writer(Charset charset) {
    return new OutputStreamWriter(outputStream(), charset);
  }

  Context after(Route.After after);

  default Context header(String name, int value) {
    return header(name, Integer.toString(value));
  }

  Context header(String name, String value);

  int status();

  Context status(int status);

  Context length(long length);

  Context type(String contentType);

  default Context write(String chunk) {
    return write(chunk, StandardCharsets.UTF_8);
  }

  Context write(String chunk, Charset charset);

  Context write(byte[] chunk);

  Context write(ByteBuffer chunk);

  default Context send(String response) {
    return send(response, StandardCharsets.UTF_8);
  }

  Context send(String response, Charset charset);

  Context send(byte[] chunk);

  Context send(ByteBuffer chunk);

  Context end();

  boolean committed();
}
