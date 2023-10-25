package org.example.myapp.conditional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.example.myapp.conditional.Bird;
import org.example.myapp.conditional.Bird.BlueJay;
import org.example.myapp.conditional.Bird.StrawberryFinch;
import org.example.myapp.conditional.BirdFactory;
import org.example.myapp.conditional.BirdWatcher;
import org.example.myapp.conditional.QualifiedBirdWatcher;
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
        .remove("finch-time")
        .publish();
  }

  @Test
  void basic() {
    // just wire everything with no test scope, mocks etc
    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(Bird.class).isEmpty());
  }

  @Test
  void jay() {
    Config.setProperty("factory", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(BirdFactory.class).isPresent());
    assertTrue(beanScope.getOptional(BlueJay.class).isPresent());
    assertTrue(beanScope.getOptional(BirdWatcher.class).isEmpty());
  }

  @Test
  void birdWatch() {
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
  }

  @Test
  void missingBeans() {
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(BirdWatcher.class).isEmpty());
  }

  @Test
  void noFactory() {
    Config.setProperty("kiwi", "somethin");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(WithAspectConditional.class).isEmpty());
    assertTrue(beanScope.getOptional(BirdFactory.class).isEmpty());
    assertTrue(beanScope.getOptional(BlueJay.class).isEmpty());
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
    assertEquals("Kiwi", beanScope.get(Bird.class).toString());
  }

  @Test
  void factoryKiwiOverride() {
    Config.setProperty("kiwi", "somethin");
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
    assertTrue(beanScope.getOptional(WithAspectConditional.class).isPresent());
    assertEquals("Kiwi", beanScope.get(Bird.class).toString());
  }

  @Test
  void factorySecondaryOverride() {
    Config.setProperty("secondary", "somethin");
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
    assertEquals("Cassowary", beanScope.get(Bird.class).toString());
  }

  @Test
  void qualifierTest() {
    final BeanScope beanScope =
        BeanScope.builder().bean("finch", Bird.class, new StrawberryFinch()).build();
    assertTrue(beanScope.getOptional(QualifiedBirdWatcher.class).isPresent());
  }

  @Test
  void metaMetaAnnotationTest() {
    Config.setProperty("finch-time", "somethin");
    Config.setProperty("factory", "bird");
    Config.setProperty("watcher", "bird");

    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(BirdWatcher.class).isPresent());
    assertEquals("StrawBerryFinch", beanScope.get(Bird.class).toString());
  }
}
