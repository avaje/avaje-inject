package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;

/** AutoCloseable target without enablePreDestroy, it is not closed by the BeanScope. */
@InjectorTarget
public class CloseableTarget implements AutoCloseable {

  @Inject InjectedThing dependency;
  int closeCount;

  @Override
  public void close() {
    closeCount++;
  }
}
