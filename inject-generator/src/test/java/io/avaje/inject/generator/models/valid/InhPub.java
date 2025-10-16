package io.avaje.inject.generator.models.valid;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.generator.models.valid.pkg_private.PubExposed;

@Factory
public class InhPub  {

  @Bean
  PubExposed exposed() {
    return new PubExposed();
  }
}
