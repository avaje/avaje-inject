package io.avaje.inject.test;

import java.util.logging.Logger;

class Log {

  private static final Logger log = Logger.getLogger("io.avaje.inject");

  static void debug(String message) {
    log.fine(message);
  }

}
