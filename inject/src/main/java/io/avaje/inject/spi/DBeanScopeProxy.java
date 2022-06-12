package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Proxy used when injecting the BeanScope.
 */
class DBeanScopeProxy implements BeanScope {

  private BeanScope delegate;

  /**
   * This happens just before the BeanScope is started.
   *
   * @param delegate The actual BeanScope
   */
  void inject(BeanScope delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T> T get(Class<T> type) {
    return delegate.get(type);
  }

  @Override
  public <T> T get(Class<T> type, String name) {
    return delegate.get(type, name);
  }

  @Override
  public <T> T get(Type type, String name) {
    return delegate.get(type, name);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> type) {
    return delegate.getOptional(type);
  }

  @Override
  public <T> Optional<T> getOptional(Type type, String name) {
    return delegate.getOptional(type, name);
  }

  @Override
  public List<Object> listByAnnotation(Class<?> annotation) {
    return delegate.listByAnnotation(annotation);
  }

  @Override
  public <T> List<T> list(Class<T> type) {
    return delegate.list(type);
  }

  @Override
  public <T> List<T> list(Type type) {
    return delegate.list(type);
  }

  @Override
  public <T> List<T> listByPriority(Class<T> type) {
    return delegate.listByPriority(type);
  }

  @Override
  public <T> List<T> listByPriority(Class<T> type, Class<? extends Annotation> priority) {
    return delegate.listByPriority(type, priority);
  }

  @Override
  public <T> Map<String, T> map(Type type) {
    return delegate.map(type);
  }

  @Override
  public List<BeanEntry> all() {
    return delegate.all();
  }

  @Override
  public boolean contains(Type type) {
    return delegate.contains(type);
  }

  @Override
  public boolean contains(String type) {
    return delegate.contains(type);
  }

  @Override
  public void close() {
    delegate.close();
  }
}
