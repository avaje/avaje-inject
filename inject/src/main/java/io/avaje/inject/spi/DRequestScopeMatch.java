package io.avaje.inject.spi;

import io.avaje.inject.RequestScopeMatch;
import io.avaje.inject.RequestScopeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.avaje.inject.spi.KeyUtil.key;

class DRequestScopeMatch<T> implements RequestScopeMatch<T> {

  private final List<String> keys;
  private final RequestScopeProvider<T> provider;

  static <T> DRequestScopeMatch<T> of(RequestScopeProvider<T> provider, Class<T> type, String name, Class<?>[] types) {
    if (name == null || types == null) {
      return new DRequestScopeMatch<>(key(type, null), provider);
    }
    List<String> keys = new ArrayList<>(types.length + 1);
    keys.add(key(type, null));
    // add the alternate keys that the provider also matches to
    for (Class<?> aClass : types) {
      keys.add(key(aClass, name));
    }
    return new DRequestScopeMatch<>(keys, provider);
  }

  private DRequestScopeMatch(List<String> keys, RequestScopeProvider<T> provider) {
    this.keys = keys;
    this.provider = provider;
  }

  private DRequestScopeMatch(String key, RequestScopeProvider<T> provider) {
    this.keys = Collections.singletonList(key);
    this.provider = provider;
  }

  @Override
  public List<String> keys() {
    return keys;
  }

  @Override
  public RequestScopeProvider<T> provider() {
    return provider;
  }
}
