package io.avaje.inject.events;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Container class for an observer method and it's information
 *
 * @param <T> the type of the event
 */
public class Observer<T> {

  private final int priority;
  private final boolean async;
  private final Consumer<T> method;
  private final String qualifierString;

  public Observer(int priority, boolean async, Consumer<T> method, String qualifierString) {
    this.priority = priority;
    this.async = async;
    this.method = method;
    this.qualifierString = qualifierString;
  }

  public int priority() {
    return priority;
  }

  void observe(T event, String qualifier) {

    if (event != null && qualifierString.equalsIgnoreCase(qualifier)) {
      if (!async) {
        method.accept(event);
      } else {
        CompletableFuture.runAsync(() -> method.accept(event));
      }
    }
  }

  CompletableFuture<Void> observeAsync(T event, String qualifier) {

    if (event != null && qualifierString.equalsIgnoreCase(qualifier)) {
      return CompletableFuture.runAsync(() -> method.accept(event));
    }
    return CompletableFuture.completedFuture(null);
  }
}
