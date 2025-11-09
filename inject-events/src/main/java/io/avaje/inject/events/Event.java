package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * Allows the application to fire events of a particular type.
 *
 * <p>Beans fire events via an instance of the <code>Event</code> abstract class, which may be
 * injected:
 *
 * <pre>{@code
 * @Inject
 * Event<LoggedInEvent> loggedInEvent;
 *
 * }</pre>
 *
 * <p>The <code>fire()</code> method accepts an event object:
 *
 * <pre>{@code
 * public void login() {
 *   ...
 *   loggedInEvent.fire(new LoggedInEvent(user));
 * }
 *
 * }</pre>
 *
 * @param <T> the type of the event object
 */
public abstract class Event<T> {

  private static final Comparator<Observer<?>> PRIORITY = Comparator.comparing(Observer::priority);
  protected final ObserverManager manager;
  protected final List<Observer<T>> observers;
  protected final String defaultQualifier;

  protected Event(ObserverManager manager, Type type) {
    this(manager, type, "");
  }

  protected Event(ObserverManager manager, Type type, String qualifier) {
    this.manager = manager;
    this.observers = manager.observersByType(type);
    this.defaultQualifier = qualifier;
  }

  /**
   * Fires an event with the specified qualifier and notifies observers.
   *
   * @param qualifier qualifier for this event
   * @param event the event object
   */
  public void fire(T event, String qualifier) {
    observers.stream()
        .sorted(PRIORITY)
        .forEach(observer -> observer.observe(event, qualifier, false));
  }

  /**
   * Fires an event with the specified qualifier and notifies asynchronous observers
   *
   * @param event the event object
   * @param qualifier the qualifier for this event
   * @return a {@link CompletableFuture} allowing further pipeline composition on the asynchronous
   *     operation.
   */
  public CompletionStage<T> fireAsync(T event, String qualifier) {
    var exceptionHandler = new CollectingExceptionHandler();
    return observers.stream()
      .sorted(PRIORITY)
      .reduce(CompletableFuture.<Void>completedFuture(null), (future, observer) ->
          future.thenRunAsync(() -> {
            try {
              observer.observe(event, qualifier, true);
            } catch (Exception e) {
              exceptionHandler.handle(e);
            }
          }, manager.asyncExecutor()),
        (future1, future2) -> future1)
      .thenApply(v -> {
        handleExceptions(exceptionHandler);
        return event;
      });
  }

  /**
   * Fires an event and notifies observers with the qualifier set for this instance.
   *
   * @param event the event object
   */
  public void fire(T event) {
    fire(event, defaultQualifier);
  }

  /**
   * Fires an event to asynchronous observers with the qualifier set for this instance.
   *
   * @param event the event object
   * @return a {@link CompletionStage} allowing further pipeline composition on the asynchronous
   *     operation.
   */
  public CompletionStage<T> fireAsync(T event) {
    return fireAsync(event, defaultQualifier);
  }

  private static final class CollectingExceptionHandler {

    private final List<Exception> throwables;

    CollectingExceptionHandler() {
      this(new ArrayList<>());
    }

    CollectingExceptionHandler(List<Exception> throwables) {
      this.throwables = throwables;
    }

    void handle(Exception throwable) {
      throwables.add(throwable);
    }

    List<Exception> handledExceptions() {
      return throwables;
    }
  }

  private void handleExceptions(CollectingExceptionHandler handler) {
    var handledExceptions = handler.handledExceptions();
    if (!handledExceptions.isEmpty()) {
      var exception =
        handledExceptions.size() == 1
          ? new CompletionException(handledExceptions.get(0))
          : new CompletionException(null);

      for (Throwable handledException : handledExceptions) {
        exception.addSuppressed(handledException);
      }
      throw exception;
    }
  }
}
