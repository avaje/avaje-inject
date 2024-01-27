package io.avaje.inject.events;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

public interface ObserverManager {

  <T> void registerObserver(boolean async, Type type, Consumer<T> observer, String qualifier);

  <T> List<Observer<T>> observers(Type eventType);
}
