package org.example.myapp.conditional;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import jakarta.inject.Singleton;

@Singleton
@RequiresBean(Fruit.class)
@RequiresProperty(value = "watcher", equalTo = "fruit")
public class FruitNinja implements Watcher {

  private final Fruit fruit;

  FruitNinja(Fruit fruit) {
    this.fruit = fruit;
  }

  @Override
  public void watch() {
    System.out.println("watching and slicing " + fruit);
  }
}
