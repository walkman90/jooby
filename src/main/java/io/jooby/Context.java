package io.jooby;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

  /**
   * **********************************************************************************************
   * **** Response methods *************************************************************************
   * **********************************************************************************************
   */

  OutputStream toOutputStream();

  default Writer toWriter() {
    return toWriter(StandardCharsets.UTF_8);
  }

  default Writer toWriter(Charset charset) {
    return new OutputStreamWriter(toOutputStream(), charset);
  }

  Context after(Route.After after);

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
