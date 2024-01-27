package io.avaje.inject.events;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Allows the application to fire events of a particular type.
 *
 * <p>Beans fire events via an instance of the <code>Event</code> abstract class, which may be
 * injected:
 *
 * <pre>
 * &#064;Inject
 * Event&lt;LoggedInEvent&gt; loggedInEvent;
 * </pre>
 *
 * <p>The <code>fire()</code> method accepts an event object:
 *
 * <pre>
 * public void login() {
 *    ...
 *    loggedInEvent.fire(new LoggedInEvent(user));
 * }
 * </pre>
 *
 * @param <T> the type of the event object
 */
public abstract class Event<T> {

  private final List<Observer<T>> observers;

  protected Event(List<Observer<T>> observers) {
    this.observers = observers;
  }

  /**
   * Fires an event with the specified qualifier and notifies observers.
   *
   * @param qualifier qualifier for this event
   * @param event the event object
   */
  public void fire(T event, String qualifier) {
    observers.stream()
        .sorted(Comparator.comparing(Observer::priority))
        .forEach(observer -> observer.observe(event, qualifier));
  }

  /**
   * Fires an event asynchronously with the specified qualifier and notifies observers
   * asynchronously.
   *
   * @param event the event object
   * @param qualifier the qualifier for this event
   * @return a {@link CompletableFuture} allowing further pipeline composition on the asynchronous
   *     operation.
   */
  public CompletableFuture<Void> fireAsync(T event, String qualifier) {

    return CompletableFuture.allOf(
        observers.stream()
            .sorted(Comparator.comparing(Observer::priority))
            .map(o -> o.observeAsync(event, qualifier))
            .toArray(CompletableFuture[]::new));
  }

  /**
   * Fires an event and notifies observers with no qualifier.
   *
   * @param qualifier qualifier for this event
   * @param event the event object
   */
  public void fire(T event) {
    fire(event, "");
  }

  /**
   * Fires an event asynchronously with the notifies observers without qualifiers
   *
   * @param event the event object
   * @param qualifier the qualifier for this event
   * @return a {@link CompletableFuture} allowing further pipeline composition on the asynchronous
   *     operation.
   */
  public CompletableFuture<Void> fireAsync(T event) {
    return fireAsync(event, "");
  }
}
