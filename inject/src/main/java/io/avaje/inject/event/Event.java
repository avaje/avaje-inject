package io.avaje.inject.event;

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
 *
 *   @Inject
 *   Event<LoggedInEvent> loggedInEvent;
 *
 * }</pre>
 *
 * <p>The <code>fire()</code> method accepts an event object:
 *
 * <pre>{@code
 *
 *   public void login() {
 *     ...
 *     loggedInEvent.fire(new LoggedInEvent(user));
 *   }
 *
 * }</pre>
 *
 * @param <T> the type of the event object
 */
public abstract class Event<T> {

  protected final List<Observer<T>> observers;

  protected Event(ObserverManager manager, Type type) {
    this.observers = manager.observersByType(type);
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
      .sorted(Comparator.comparing(Observer::priority))
      .reduce(CompletableFuture.<Void>completedFuture(null), (future, observer) ->
          future.thenRun(() -> {
            try {
              observer.observe(event, qualifier, true);
            } catch (Exception e) {
              exceptionHandler.handle(e);
            }
          }),
        (future1, future2) -> future1)
      .thenApply(v -> {
        handleExceptions(exceptionHandler);
        return event;
      });
  }

  /**
   * Fires an event and notifies observers with no qualifier.
   *
   * @param event the event object
   */
  public void fire(T event) {
    fire(event, "");
  }

  /**
   * Fires an event to asynchronous observers without qualifiers
   *
   * @param event the event object
   * @return a {@link CompletionStage} allowing further pipeline composition on the asynchronous
   *     operation.
   */
  public CompletionStage<T> fireAsync(T event) {
    return fireAsync(event, "");
  }

  private static class CollectingExceptionHandler {

    private final List<Exception> throwables;

    CollectingExceptionHandler() {
      this(new ArrayList<>());
    }

    CollectingExceptionHandler(List<Exception> throwables) {
      this.throwables = throwables;
    }

    public void handle(Exception throwable) {
      throwables.add(throwable);
    }

    public List<Exception> getHandledExceptions() {
      return throwables;
    }
  }

  private void handleExceptions(CollectingExceptionHandler handler) {
    var handledExceptions = handler.getHandledExceptions();
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
