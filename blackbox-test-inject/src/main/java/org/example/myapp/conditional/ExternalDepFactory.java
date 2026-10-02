package org.example.myapp.conditional;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

/**
 * Conditional factory whose bean method parameters are treated as external.
 * See https://github.com/avaje/avaje-inject/issues/1081
 */
@Factory
@RequiresBean(ExternalDep.class)
public class ExternalDepFactory {

  @Bean
  ExternalDepService externalDepService(ExternalDep dep) {
    return new ExternalDepService(dep);
  }
}
