package io.avaje.inject.generator.models.valid.beantypes;

import java.io.Serializable;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;

@Factory
public class BeanTypesFactory {

  @Bean
  @BeanTypes(Serializable.class)
  Optional<String> anOptional() {
    // intentional for testing optional dependency
    return Optional.empty();
  }

  @Bean
  @BeanTypes(CharSequence.class)
  @Nullable String nullable() {
    return null;
  }
}
