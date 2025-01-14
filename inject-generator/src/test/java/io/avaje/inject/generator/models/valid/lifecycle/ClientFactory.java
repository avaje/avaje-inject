package io.avaje.inject.generator.models.valid.lifecycle;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class ClientFactory {

  @Bean
  ClientInterface i() {
    return null;
  }
}
