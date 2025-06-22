package io.avaje.inject.generator.models.valid.lazy.generic;

import io.avaje.inject.BeanTypes;
import io.avaje.inject.Lazy;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Lazy
@Singleton
@BeanTypes(LazyGenericInterface.class)
public class LazyGenericBeanTypes implements LazyGenericInterface {

  Provider<Integer> intProvider;

  @Inject
  public LazyGenericBeanTypes(Provider<Integer> intProvider) {
    this.intProvider = intProvider;
  }

  @Override
  public void something() {}

  @Override
  public String somethingElse() {
    return null;
  }

  @Override
  public Object gen() {
    return null;
  }
}
