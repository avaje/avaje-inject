package io.avaje.inject.generator.models.valid;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
public class IncorrectCircularDependency {

  @Bean
  @Named("parent") // for clarity, not necessary
  public String parent(@Named("child") String child) {
    throw new AssertionError("Method body unimportant");
  }

  @Bean
  @Named("child")
  public String child() {
    throw new AssertionError("Method body unimportant");
  }

//  @Bean compilation rightly fails when this is uncommented
//  public String child1(@Named("child2") String child) {
//    throw new AssertionError("Method body unimportant");
//  }
}
