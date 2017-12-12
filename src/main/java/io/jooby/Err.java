package io.jooby;

public class Err extends RuntimeException {

  private int status;

  public Err(int status, String message, Throwable cause) {
    super(status + tail(message), cause);
    this.status = status;
  }

  public Err(int status, String message) {
    this(status, message, null);
  }

  public Err(int status) {
    this(status, (String) null);
  }

  public Err(int status, Throwable cause) {
    this(status, null, cause);
  }

  public Err(Status status, String message) {
    this(status, message, null);
  }

  public Err(Status status) {
    this(status, (String)null);
  }

  public Err(Status status, Throwable cause) {
    this(status, null, cause);
  }

  public Err(Status status, String message, Throwable cause) {
    super(status + tail(message), cause);
    this.status = status.value();
  }

  public int status() {
    return status;
  }

  private static String tail(String message) {
    return message == null ? "" : ": " + message;
  }
}
