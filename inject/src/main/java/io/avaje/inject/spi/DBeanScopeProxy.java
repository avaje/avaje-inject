package io.avaje.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;

/** Proxy used when injecting the BeanScope. */
final class DBeanScopeProxy implements BeanScope {

  private Builder builder;
  private BeanScope delegate;

  /**
   * Will use the builder as a delegate until the scope is fully built;
   */
  public DBeanScopeProxy(Builder builder) {
    this.builder = builder;
  }

  /**
   * This happens just before the BeanScope is started.
   *
   * @param delegate The actual BeanScope
   */
  void inject(BeanScope delegate) {
    this.delegate = delegate;
    builder = null;
  }

  @Override
  public <T> T get(Class<T> type) {
    if (delegate != null) {
      return delegate.get(type);
    } else {
      return builder.get(type);
    }
  }

  @Override
  public <T> T get(Class<T> type, String name) {
    if (delegate != null) {
      return delegate.get(type, name);
    } else {
      return builder.get(type, name);
    }
  }

  @Override
  public <T> T get(Type type, String name) {
    if (delegate != null) {
      return delegate.get(type, name);
    } else {
      return builder.get(type, name);
    }
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> type) {
    if (delegate != null) {
      return delegate.getOptional(type);
    } else {
      return builder.getOptional(type);
    }
  }

  @Override
  public <T> Optional<T> getOptional(Type type, String name) {
    if (delegate != null) {
      return delegate.getOptional(type, name);
    } else {
      return builder.getOptional(type, name);
    }
  }

  @Override
  public List<Object> listByAnnotation(Class<? extends Annotation> annotation) {
    if (delegate != null) {
      return delegate.listByAnnotation(annotation);
    } else {
      return builder.listByAnnotation(annotation);
    }
  }

  @Override
  public <T> List<T> list(Class<T> type) {

    if (delegate != null) {
      return delegate.list(type);
    } else {
      return builder.list(type);
    }
  }

  @Override
  public <T> List<T> list(Type type) {
    if (delegate != null) {
      return delegate.list(type);
    } else {
      return builder.list(type);
    }
  }

  @Override
  public <T> List<T> listByPriority(Class<T> type) {
    if (delegate != null) {
      return delegate.listByPriority(type);
    } else {
      throw new IllegalStateException(
          "Proxy BeanScope can't use listByPriority while scope is being built");
    }
  }

  @Override
  public <T> List<T> listByPriority(Class<T> type, Class<? extends Annotation> priority) {
    if (delegate != null) {
      return delegate.listByPriority(type, priority);
    } else {
      throw new IllegalStateException(
          "Proxy BeanScope can't use listByPriority while scope is being built");
    }
  }

  @Override
  public <T> Map<String, T> map(Type type) {

    if (delegate != null) {
      return delegate.map(type);
    } else {
      return builder.map(type);
    }
  }

  @Override
  public List<BeanEntry> all() {
    if (delegate != null) {
      return delegate.all();
    } else {
      throw new IllegalStateException("Proxy BeanScope can't use all() while scope is being built");
    }
  }

  @Override
  public boolean contains(Type type) {
    if (delegate != null) {
      return delegate.contains(type);
    } else {
      throw new IllegalStateException(
          "Proxy BeanScope can't use contains() while scope is being built, try get(T) != null");
    }
  }

  @Override
  public boolean contains(String type) {
    if (delegate != null) {
      return delegate.contains(type);
    } else {
      throw new IllegalStateException(
          "Proxy BeanScope can't use contains() while scope is being built, try get(T) != null");
    }
  }

  @Override
  public void close() {
    if (delegate != null) {
      delegate.close();
    } else {
      throw new IllegalStateException(
          "Proxy BeanScope can't use close() while scope is being built");
    }
  }

}
