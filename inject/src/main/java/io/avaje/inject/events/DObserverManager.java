package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class DObserverManager implements ObserverManager {

  Map<Type, List<Observer<?>>> observeMap = new HashMap<>();

  @Override
  public <T> void registerObserver(
      boolean sync, Type type, Consumer<T> observer, String qualifier) {

    observeMap
        .computeIfAbsent(type, k -> new ArrayList<>())
        .add(new Observer<>(sync, observer, qualifier));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Observer<?>> observers(Type eventType) {

    return observeMap.computeIfAbsent(eventType, k -> new ArrayList<>());
  }
}
