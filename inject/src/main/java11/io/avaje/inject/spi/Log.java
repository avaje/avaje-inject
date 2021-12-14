package io.avaje.inject.spi;

class Log {

  private static final System.Logger log = System.getLogger("io.avaje.inject");

  static void trace(String message) {
    log.log(System.Logger.Level.TRACE, message);
  }

  static void error(String message, Exception e) {
    log.log(System.Logger.Level.ERROR, message, e);
  }
}
