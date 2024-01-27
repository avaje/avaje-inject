package io.avaje.inject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.avaje.inject.events.Observer;
import io.avaje.inject.events.ObserverManager;

class DObserverManager implements ObserverManager {

  Map<Type, List<Observer<?>>> observeMap = new HashMap<>();

  @Override
  public <T> void registerObserver(Type type, Observer<T> observer) {

    observeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(observer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Observer<?>> observersByType(Type eventType) {

    return observeMap.computeIfAbsent(eventType, k -> new ArrayList<>());
  }
}
