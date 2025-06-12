package io.avaje.inject.generator.models.valid.lazy;

import io.avaje.inject.BeanTypes;
import io.avaje.inject.Lazy;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Lazy
@Singleton
@BeanTypes(LazyInterface.class)
public class LazyBeanTypes implements LazyInterface {

  Provider<Integer> intProvider;

  @Inject
  public LazyBeanTypes(Provider<Integer> intProvider) {
    this.intProvider = intProvider;
  }

  @Override
  public void something() {}
}
