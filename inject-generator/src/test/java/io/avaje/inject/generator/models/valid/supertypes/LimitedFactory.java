package io.avaje.inject.generator.models.valid.supertypes;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;

@Factory
public class LimitedFactory {

  @Bean
  @BeanTypes(SomeInterface.class)
  OtherComponent bean() {
    return new OtherComponent();
  }
}
