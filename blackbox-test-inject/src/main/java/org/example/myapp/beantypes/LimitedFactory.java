package org.example.myapp.beantypes;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import jakarta.inject.Named;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Optional;

@Factory
public class LimitedFactory {

  @Bean
  @Named("factory")
  @BeanTypes(LimitedInterface.class)
  BeanTypeComponent bean() {
    return new BeanTypeComponent();
  }

  @Bean
  @BeanTypes(Serializable.class)
  Optional<String> anOptional() {
    // intentional for testing optional dependency
    return Optional.of("IAmSerializable");
  }

  @Bean
  @BeanTypes(CharSequence.class)
  @Nullable String nullable() {
    return "IAmNullable";
  }
}
