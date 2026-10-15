package org.example.myapp.conditional;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

/** {@code @External} applied via a meta-annotation on the bean method. */
@Factory
public class MetaExternalDepFactory {

  @Bean
  @RequiresMetaExternalDep
  MetaExternalDepService metaExternalDepService(MetaExternalDep dep) {
    return new MetaExternalDepService(dep);
  }
}
