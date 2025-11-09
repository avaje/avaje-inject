package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

final class DObserverManager implements ObserverManager {

  private final Map<Type, List<Observer<?>>> observeMap = new HashMap<>();
  private final Executor executor;

  DObserverManager(Executor executor) {
    this.executor = executor;
  }

  @Override
  public <T> void registerObserver(Type type, Observer<T> observer) {
    observeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(observer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Observer<?>> observersByType(Type eventType) {
    return observeMap.computeIfAbsent(eventType, k -> new ArrayList<>());
  }

  @Override
  public Executor asyncExecutor() {
    return executor;
  }
}
