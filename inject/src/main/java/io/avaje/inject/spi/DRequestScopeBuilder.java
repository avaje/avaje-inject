package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;
import io.avaje.inject.RequestScope;
import io.avaje.inject.RequestScopeBuilder;

import java.util.concurrent.ConcurrentHashMap;

import static io.avaje.inject.spi.KeyUtil.key;

/**
 * Builder for request scope.
 */
class DRequestScopeBuilder implements RequestScopeBuilder {

  private final ConcurrentHashMap<String, Object> supplied = new ConcurrentHashMap<>();

  private final BeanScope beanScope;

  DRequestScopeBuilder(BeanScope beanScope) {
    this.beanScope = beanScope;
  }

  @Override
  public <D> RequestScopeBuilder withBean(Class<D> type, D bean) {
    return withBean(null, type, bean);
  }

  @Override
  public <D> RequestScopeBuilder withBean(String name, Class<D> type, D bean) {
    supplied.put(key(type, name), bean);
    return this;
  }

  @Override
  public RequestScope build() {
    return new DRequestScope(beanScope, supplied);
  }
}
