package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloServiceTest {

  static {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }

  void asd() {
    org.slf4j.Logger slf4j = LoggerFactory.getLogger("io.avaje.doo2");
    slf4j.trace("trace doo2");
  }
  /**
   * No mocking, no use of <code>@TestScope</code> so just like main.
   */
  @Test
  void basic() {

    asd();
    Logger utilLogger = Logger.getLogger("io.avaje.doo1");
    utilLogger.log(Level.FINE, "fine doo");
    utilLogger.log(Level.FINER, "finer doo");
    utilLogger.log(Level.INFO, "info doo");

    // just wire everything with no test scope, mocks etc
    BeanScope beanScope = BeanScope.newBuilder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    assertEquals("hello+AppHelloData", helloService.hello());
  }
}
