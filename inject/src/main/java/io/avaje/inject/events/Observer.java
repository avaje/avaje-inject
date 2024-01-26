package io.avaje.inject.events;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Observer<T> {

  final boolean sync;
  final Consumer<T> method;
  final String qualifierString;

  public Observer(boolean sync, Consumer<T> method, String qualifierString) {
    this.sync = sync;
    this.method = method;
    this.qualifierString = qualifierString;
  }

  void observe(T event, String qualifier) {

    if (event != null && qualifierString.isBlank() || qualifierString.equalsIgnoreCase(qualifier)) {
      if (sync) {
        method.accept(event);
      } else {
        CompletableFuture.runAsync(() -> method.accept(event));
      }
    }
  }

  CompletableFuture<Void> observeAsync(T event, String qualifier) {

    if (event != null && qualifierString.isBlank() || qualifierString.equalsIgnoreCase(qualifier)) {
      return CompletableFuture.runAsync(() -> method.accept(event));
    }
    return CompletableFuture.completedFuture(null);
  }
}
