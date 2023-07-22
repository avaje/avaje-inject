package org.example.myapp.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.example.myapp.profile.Fish.CardinalTetra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest(profiles = {"Fish", "testRepeatable"})
class ProfileTests {

  @Inject FishFactory factory;

  @BeforeEach
  void clearConfig() {

    Config.clearProperty("avaje.profiles");
  }

  @Test
  void basic() throws IOException {

    assertNotNull(factory != null);

    // just wire everything with no test scope, mocks etc
    final BeanScope beanScope = BeanScope.builder().build();
    assertTrue(beanScope.getOptional(Fish.class).isEmpty());
  }

  @Test
  void tetra() throws IOException {

    Config.setProperty("avaje.profiles", "Fish,testRepeatable");

    final BeanScope beanScope = BeanScope.builder().build();

    assertTrue(beanScope.getOptional(FishFactory.class).isPresent());
    assertTrue(beanScope.getOptional(CardinalTetra.class).isPresent());
  }

  @Test
  void metaMetaAnnotationTest() throws IOException {
    Config.setProperty("avaje.profiles", "factory,betta-time,testRepeatable");

    final BeanScope beanScope = BeanScope.builder().build();
    assertEquals("Betta", beanScope.get(Fish.class).toString());
  }
}
