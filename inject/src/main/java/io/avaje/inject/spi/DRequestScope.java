package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;
import io.avaje.inject.RequestScope;
import io.avaje.inject.RequestScopeMatch;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.avaje.inject.spi.KeyUtil.key;

/**
 * Implementation of RequestScope.
 */
class DRequestScope implements RequestScope {

  private static final Logger log = LoggerFactory.getLogger(RequestScope.class);

  private final BeanScope beanScope;
  private final ConcurrentHashMap<String, Object> supplied;
  private final List<Closeable> closeables = new ArrayList<>();

  DRequestScope(BeanScope beanScope, ConcurrentHashMap<String, Object> supplied) {
    this.beanScope = beanScope;
    this.supplied = supplied;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getNullable(Class<T> type, String name) {
    String key = key(type, name);
    Object suppliedBean = supplied.get(key);
    if (suppliedBean != null) {
      return (T) suppliedBean;
    }
    RequestScopeMatch<T> reqMatch = beanScope.requestProvider(type, name);
    if (reqMatch != null) {
      return createRequestScopeBean(reqMatch);
    }
    return beanScope.get(type, name);
  }

  @SuppressWarnings("unchecked")
  private <T> T createRequestScopeBean(RequestScopeMatch<T> reqMatch) {
    Object createdBean = reqMatch.provider().provide(this);
    for (String matchKey : reqMatch.keys()) {
      supplied.put(matchKey, createdBean);
    }
    return (T) createdBean;
  }

  @Override
  public <T> T getNullable(Class<T> type) {
    return getNullable(type, null);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> type) {
    return Optional.of(getNullable(type));
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> type, String name) {
    return Optional.of(getNullable(type, name));
  }

  @Override
  public <T> T get(Class<T> type) {
    return get(type, null);
  }

  @Override
  public <T> T get(Class<T> type, String name) {
    T maybe = getNullable(type, name);
    if (maybe == null) {
      throw new IllegalStateException("Missing bean for type:" + type + " name:" + name);
    }
    return maybe;
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> type) {
    return getProvider(type, null);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> type, String name) {
    // in request scope everything is already wired, there is no delay
    return () -> get(type, name);
  }

  @Override
  public <T> List<T> list(Class<T> interfaceType) {
    return beanScope.list(interfaceType);
  }

  @Override
  public <T> Set<T> set(Class<T> interfaceType) {
    return new LinkedHashSet<>(beanScope.list(interfaceType));
  }

  @Override
  public void addClosable(Closeable closeable) {
    closeables.add(closeable);
  }

  @Override
  public void close() {
    for (Closeable closeable : closeables) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.error("Error closing request scoped bean", e);
      }
    }
  }
}
