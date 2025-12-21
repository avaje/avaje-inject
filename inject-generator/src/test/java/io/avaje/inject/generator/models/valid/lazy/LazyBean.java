package io.avaje.inject.generator.models.valid.lazy;

import java.io.IOException;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Lazy;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import io.avaje.inject.Primary;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Lazy
@Singleton
@Primary
public class LazyBean {

  Provider<Integer> intProvider;

  @Inject
  public LazyBean(Provider<Integer> intProvider) {
    this.intProvider = intProvider;
  }

  @PostConstruct
  void init(BeanScope scope) {}

  @PreDestroy
  void shutdown() {}

  public LazyBean() {}

  void something() throws IOException {}
}
