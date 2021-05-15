package org.example.requestscope;

import io.avaje.inject.PreDestroy;
import io.avaje.inject.Request;

@Request
public class MyRequestPreDestroy {

  private boolean firedPreDestroy;

  /**
   * Using PreDestroy rather than implementing Closable.
   */
  @PreDestroy
  void destroy() {
    firedPreDestroy = true;
  }

  boolean isFiredPreDestroy() {
    return firedPreDestroy;
  }
}
