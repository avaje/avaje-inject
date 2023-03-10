package org.example.myapp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.example.myapp.conditional.Bird;
import org.example.myapp.conditional.Bird.Jay;
import org.example.myapp.conditional.BirdFactory;
import org.example.myapp.conditional.BirdWatcher;
import org.example.myapp.conditional.Fruit.Apple;
import org.example.myapp.conditional.FruitNinja;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;

class ConditionalTests {

  @BeforeEach
  void clearConfig() {

    Config.eventBuilder("")
        .remove("secondary")
        .remove("factory")
        .remove("watcher")
        .remove("kiwi")
        .publish();
  }

  @Test
  void basic() throws IOException {
    // just wire everything with no test scope, mocks etc
    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(Bird.class).isEmpty());
  }

  @Test
  void jay() throws IOException {

    Config.setProperty("factory", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(BirdFactory.class).isPresent());
    assertTrue(beanScope.getOptional(Jay.class).isPresent());
  }

  @Test
  void birdWatch() throws IOException {

    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
  }

  @Test
  void fruitWatch() throws IOException {

    Config.setProperty("factory", "fruit");
    Config.setProperty("watcher", "fruit");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(FruitNinja.class).isPresent());
  }

  @Test
  void wrongFactory() throws IOException {

    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "fruit");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(FruitNinja.class).isEmpty());
  }

  @Test
  void noFactory() throws IOException {

    Config.setProperty("kiwi", "somethin");
    Config.setProperty("watcher", "fruit");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(FruitNinja.class).isPresent());
    assertTrue(beanScope.getOptional(Apple.class).isEmpty());
  }

  @Test
  void factoryKiwiOverride() throws IOException {

    Config.setProperty("kiwi", "somethin");
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());

    assertTrue(beanScope.get(Bird.class).toString().equals("Kiwi"));
  }

  @Test
  void factorySecondaryOverride() throws IOException {

    Config.setProperty("secondary", "somethin");
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());

    assertTrue(beanScope.get(Bird.class).toString().equals("Cassowary"));
  }
}
