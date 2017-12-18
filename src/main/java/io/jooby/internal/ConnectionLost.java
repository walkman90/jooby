package io.jooby.internal;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

public class ConnectionLost {

  public static boolean test(final Throwable cause) {
    if (cause instanceof IOException) {
      String message = cause.getMessage();
      if (message != null && (message.indexOf("reset by peer") != -1
          || message.indexOf("Broken pipe") != -1)) {
        return true;
      }
    }
    if (cause instanceof ClosedChannelException) {
      return true;
    }
    return false;
  }
}
