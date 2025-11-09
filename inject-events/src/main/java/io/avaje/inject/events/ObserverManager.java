package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Manages all {@link Observer} instances in the BeanScope.
 *
 * <p>A default implementation is provided by avaje-inject.
 */
public interface ObserverManager {

  /**
   * Registers the given Consumer as an observer.
   *
   * @param <T> the type of the event
   * @param eventType the type of the event ()
   * @param observer the consumer to execute when a matching event is found
   */
  <T> void registerObserver(Type eventType, Observer<T> observer);

  /**
   * Retrieves a list of all Observers registered by the given type
   *
   * @param <T> the Type of the Event
   * @param eventType the type of the event
   * @return all observers registered
   */
  <T> List<Observer<T>> observersByType(Type eventType);

  /** The Executor used for sending async events */
  Executor asyncExecutor();
}
