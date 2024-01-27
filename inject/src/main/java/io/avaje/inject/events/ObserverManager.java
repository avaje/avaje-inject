package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

/** Manages all {@link Observer} instances in the BeanScope */
public interface ObserverManager {

  /**
   * Registers the given Consumer as an observer.
   *
   * @param <T> the type of the event
   * @param async whether this observer should exclusively executed asynchronously
   * @param type the type of the event ()
   * @param observer the consumer to execute when a matching event is found
   * @param qualifier qualifier string that this observer should be registered to
   */
  <T> void registerObserver(boolean async, Type type, Consumer<T> observer, String qualifier);

  /**
   * Retrieves a list of all Observers registered by the given type
   *
   * @param <T> the Type of the Event
   * @param eventType the type of the event
   * @return all observers registered
   */
  <T> List<Observer<T>> observersByType(Type eventType);
}
