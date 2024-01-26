package io.avaje.inject.events;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Observer<T> {

  private final boolean sync;
  private final Consumer<T> method;
  private final String qualifierString;

  public Observer(boolean sync, Consumer<T> method, String qualifierString) {
    this.sync = sync;
    this.method = method;
    this.qualifierString = qualifierString;
  }

  void observe(T event, String qualifier) {

    if (event != null && qualifierString.isBlank() || qualifierString.equalsIgnoreCase(qualifier)) {
      if (sync) {
        execute(event);
      } else {
        CompletableFuture.runAsync(() -> execute(event));
      }
    }
  }

  CompletableFuture<Void> observeAsync(T event, String qualifier) {

    if (event != null && qualifierString.isBlank() || qualifierString.equalsIgnoreCase(qualifier)) {
      return CompletableFuture.runAsync(() -> execute(event));
    }
    return CompletableFuture.completedFuture(null);
  }

  private void execute(T event) {
    method.accept(event);
  }
}
