package org.example.myapp.conditional;

import org.example.myapp.conditional.Bird.Jay;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.Secondary;

@Factory
@RequiresProperty(value = "factory", equalTo = "bird")
public class BirdFactory {

  @Bean
  @NoKiwi
  public Jay jay() {
    return new Jay();
  }

  @Bean
  @Secondary
  public Cassowary dinosaur() {
    return new Cassowary();
  }
}
