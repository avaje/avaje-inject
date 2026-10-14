package org.example.myapp.conditional;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresBean;

/**
 * Conditional factory with {@code @External} on the bean method applying to all its parameters.
 * See https://github.com/avaje/avaje-inject/issues/1081
 */
@Factory
@RequiresBean(ExternalDep.class)
public class ExternalDepFactory {

  @Bean
  @External
  ExternalDepService externalDepService(ExternalDep dep) {
    return new ExternalDepService(dep);
  }
}
