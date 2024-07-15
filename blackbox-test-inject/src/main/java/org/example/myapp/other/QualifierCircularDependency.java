package org.example.myapp.other;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
public class QualifierCircularDependency {

  @Bean
  @Named("parent") // for clarity, not necessary
  public String parent(@Named("child") String child) {
    return "parent-"+ child;
  }

  @Bean
  @Named("child")
  public String child() {
    return "child";
  }

//  @Bean compilation rightly fails when this is uncommented
//  public String child1(@Named("child2") String child) {
//    throw new AssertionError("Method body unimportant");
//  }
}
