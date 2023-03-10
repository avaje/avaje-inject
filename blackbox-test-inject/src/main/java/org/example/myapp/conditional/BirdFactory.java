package org.example.myapp.conditional;

import org.example.myapp.conditional.Bird.BlueJay;
import org.example.myapp.conditional.Bird.Cassowary;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.Secondary;

@Factory
@RequiresProperty(value = "factory", equalTo = "bird", missingProperties = "neverExisted")
public class BirdFactory {

  @Bean
  @NoKiwi
  public BlueJay jay() {
    return new BlueJay();
  }

  @Bean
  @Secondary
  public Cassowary dinosaur() {
    return new Cassowary();
  }

}
