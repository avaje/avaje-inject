package io.avaje.inject.generator.models.valid.lazy;

import java.io.IOException;

import io.avaje.inject.Lazy;
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

  public LazyBean() {}

  void something() throws IOException {}
}
