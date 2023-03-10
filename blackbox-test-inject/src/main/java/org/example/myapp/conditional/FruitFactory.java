package org.example.myapp.conditional;

import org.example.myapp.conditional.Fruit.Apple;
import org.example.myapp.conditional.Fruit.Mango;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.Secondary;

@Factory
@RequiresProperty(value = "factory", equalTo = "fruit")
public class FruitFactory {

  @Bean
  @NoKiwi
  public Fruit apple() {
    return new Apple();
  }

  @Bean
  @Secondary
  public Fruit mango() {
    return new Mango();
  }
}
