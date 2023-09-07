package io.avaje.inject.generator.models.valid;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
public class UserMaker {
  @Bean
  @Named("User-  ;():	' []{}/*&^%$#@<>?|+-= Id")
  String userId() {
    return "bla bla";
  }
}
