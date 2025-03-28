package org.example.myapp.conditional;

import org.example.myapp.conditional.Bird.BlueJay;
import org.example.myapp.conditional.Bird.Cassowary;
import org.example.myapp.conditional.Bird.StrawberryFinch;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.Secondary;

@Factory
@RequiresProperty(value = "factory", equalTo = "bird", missing = "neverExisted")
@RequiresProperty(value = "somethingElse", notEqualTo = "testRepeatable")
public class BirdFactory {

  @Bean
  @NoKiwi
  @RequiresProperty(missing = "finch-time")
  public Bird jay() {
    return new BlueJay();
  }

  @Bean
  @Secondary
  public Bird dinosaur() {
    return new Cassowary();
  }

  @Bean
  @Finches
  public Bird finch() {
    return new StrawberryFinch();
  }
}
