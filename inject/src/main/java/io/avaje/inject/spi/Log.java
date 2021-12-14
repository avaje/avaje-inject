package io.avaje.inject.spi;

import java.util.logging.Level;
import java.util.logging.Logger;

class Log {
  private static final Logger log = Logger.getLogger("io.avaje.inject");

  static void trace(String message) {
    log.log(Level.FINER, message);
  }

  static void error(String message, Exception e) {
    log.log(Level.SEVERE, message, e);
  }
}
