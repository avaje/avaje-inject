package io.avaje.inject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.avaje.inject.events.Observer;
import io.avaje.inject.events.ObserverManager;

class DObserverManager implements ObserverManager {

  Map<Type, List<Observer<?>>> observeMap = new HashMap<>();

  @Override
  public <T> void registerObserver(
      boolean async, Type type, Consumer<T> observer, String qualifier) {

    observeMap
        .computeIfAbsent(type, k -> new ArrayList<>())
        .add(new Observer<>(async, observer, qualifier));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Observer<?>> observersByType(Type eventType) {

    return observeMap.computeIfAbsent(eventType, k -> new ArrayList<>());
  }
}
