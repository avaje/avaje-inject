package io.avaje.inject.generator.models.valid.lazy.generic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import io.avaje.inject.generator.models.valid.lazy.LazyBeanPkgPrivate;

@Factory
public class LazyGenericFactory {

  @Bean
  @Lazy
  LazyGenericInterface<String> lazyInterface() {
    return null;
  }

  @Bean
  @Lazy
  LazyBeanPkgPrivate pkgPrivateMethods() {
    return null;
  }
}
