package io.avaje.inject.events.spi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.events.Observer;
import io.avaje.inject.events.ObserverManager;

final class DObserverManager implements ObserverManager {

  private final Map<Type, List<Observer<?>>> observeMap = new HashMap<>();
  private Executor executor = ForkJoinPool.commonPool();

  @PostConstruct
  void post(Executor executor) {
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
