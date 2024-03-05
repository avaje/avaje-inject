package io.avaje.inject.event;

import java.util.function.Consumer;

/**
 * Container class for an observer method and its information
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

  void observe(T event, String qualifier, boolean async) {

    if (this.async == async && event != null && qualifierString.equalsIgnoreCase(qualifier))
      method.accept(event);
  }
}
