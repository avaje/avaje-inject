package io.avaje.inject.spi;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;

import java.lang.System.Logger.Level;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.avaje.applog.AppLog;
import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;

@NullMarked
final class DBeanScope implements BeanScope {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");

  private final ReentrantLock lock = new ReentrantLock();
  private final List<Runnable> postConstruct;
  private final List<Consumer<BeanScope>> postConstructConsumers;
  private final List<AutoCloseable> preDestroy;
  private final DBeanMap beans;
  private final ShutdownHook shutdownHook;
  private final BeanScope parent;
  private boolean shutdown;
  private boolean closed;

  DBeanScope(
      boolean withShutdownHook,
      List<AutoCloseable> preDestroy,
      List<Runnable> postConstruct,
      List<Consumer<BeanScope>> postConstructConsumers,
      DBeanMap beans,
      BeanScope parent) {
    this.preDestroy = preDestroy;
    this.postConstruct = postConstruct;
    this.postConstructConsumers = postConstructConsumers;
    this.beans = beans;
    this.parent = parent;
    if (withShutdownHook) {
      this.shutdownHook = new ShutdownHook(this);
      Runtime.getRuntime().addShutdownHook(shutdownHook);
    } else {
      this.shutdownHook = null;
    }
  }

  @Override
  public String toString() {
    return "BeanScope{" + beans + '}';
  }

  @Override
  public List<BeanEntry> all() {
    final IdentityHashMap<DContextEntryBean, DEntry> map = new IdentityHashMap<>();
    if (parent != null) {
      ((DBeanScope) parent).addAll(map);
    }
    addAll(map);
    return new ArrayList<>(map.values());
  }

  void addAll(Map<DContextEntryBean, DEntry> map) {
    beans.addAll(map);
  }

  @Override
  public boolean contains(String type) {
    return beans.contains(type);
  }

  @Override
  public boolean contains(Type type) {
    return beans.contains(type);
  }

  @Override
  public <T> T get(Class<T> type) {
    return getByType(type, null);
  }

  @Override
  public <T> T get(Class<T> type, @Nullable String name) {
    return getByType(type, name);
  }

  @Override
  public <T> T get(Type type, @Nullable String name) {
    return getByType(type, name);
  }

  private <T> T getByType(Type type, @Nullable String name) {
    final T bean = beans.get(type, name);
    if (bean != null) {
      return bean;
    }
    if (parent == null) {
      throw new NoSuchElementException("No bean found for type: " + type + " name: " + name);
    }
    return parent.get(type, name);
  }

  /**
   * Get with a strict match on name for the single entry case.
   */
  @Nullable
  Object getStrict(String name, Type[] types) {
    for (Type type : types) {
      Object match = beans.getStrict(type, name);
      if (match != null) {
        return match;
      }
    }
    if (parent instanceof DBeanScope) {
      DBeanScope dParent = (DBeanScope) parent;
      return dParent.getStrict(name, types);
    }
    return null;
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> type) {
    return getMaybe(type, null);
  }

  @Override
  public <T> Optional<T> getOptional(Type type, @Nullable String name) {
    return getMaybe(type, name);
  }

  private <T> Optional<T> getMaybe(Type type, @Nullable String name) {
    final T bean = beans.get(type, name);
    if (bean != null) {
      return Optional.of(bean);
    }
    if (parent == null) {
      return Optional.empty();
    }
    return parent.getOptional(type, name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Map<String, T> map(Type type) {
    return (Map<String, T>) beans.map(type, parent);
  }

  @Override
  public <T> List<T> list(Class<T> type) {
    return listOf(type);
  }

  @Override
  public <T> List<T> list(Type type) {
    return listOf(type);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> listOf(Type type) {
    List<T> values = (List<T>) beans.all(type);
    if (parent == null) {
      return values;
    }
    return combine(values, parent.list(type));
  }

  static <T> List<T> combine(List<T> values, List<T> parentValues) {
    if (values.isEmpty()) {
      return parentValues;
    } else if (parentValues.isEmpty()) {
      return values;
    } else {
      values.addAll(parentValues);
      return values;
    }
  }

  @Override
  public <T> List<T> listByPriority(Type type) {
    return beans.listByPriority(type);
  }

  @Override
  public List<Object> listByAnnotation(Class<? extends Annotation> annotation) {
    final List<Object> values = all().stream()
    	      .filter(entry -> entry.type().isAnnotationPresent(annotation))
    	      .map(BeanEntry::bean)
    	      .collect(Collectors.toList());
    if (parent == null) {
      return values;
    }
    return combine(values, parent.listByAnnotation(annotation));
  }

  DBeanScope start(long start) {
    lock.lock();
    try {
      log.log(TRACE, "firing postConstruct");
      for (final var invoke : postConstruct) {
        invoke.run();
      }
      for (final var consumer : postConstructConsumers) {
        consumer.accept(this);
      }
    } finally {
      lock.unlock();
    }
    log.log(INFO, "Wired beans in {0}ms", System.currentTimeMillis() - start);
    return this;
  }

  @Override
  public void close() {
    lock.lock();
    try {
      if (shutdownHook != null && !shutdown) {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
      }
      if (!closed) {
        // we only allow one call to preDestroy
        closed = true;
        log.log(TRACE, "firing preDestroy");
        for (final AutoCloseable closeable : preDestroy) {
          try {
            closeable.close();
          } catch (final Exception e) {
            log.log(Level.ERROR, "Error during PreDestroy lifecycle method", e);
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private void shutdown() {
    lock.lock();
    try {
      shutdown = true;
      close();
    } finally {
      lock.unlock();
    }
  }

  private static class ShutdownHook extends Thread {
    private final DBeanScope scope;

    ShutdownHook(DBeanScope scope) {
      this.scope = scope;
    }

    @Override
    public void run() {
      scope.shutdown();
    }
  }
}
