package io.avaje.inject.events;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Event<T> {

  private final List<Observer<T>> observers;

  protected Event(List<Observer<T>> observers) {
    this.observers = observers;
  }

  public void fire(T event, String qualifiers) {
    for (var observer : observers) {
      observer.observe(event, qualifiers);
    }
  }

  public List<CompletableFuture<Void>> fireAsync(T event, String qualifiers) {

    return observers.stream().map(o -> o.observeAsync(event, qualifiers)).collect(toList());
  }

  public void fire(T event) {
    fire(event, "");
  }

  public List<CompletableFuture<Void>> fireAsync(T event) {
    return fireAsync(event, "");
  }
}
