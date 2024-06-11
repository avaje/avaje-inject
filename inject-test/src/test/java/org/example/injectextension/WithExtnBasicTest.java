package org.example.injectextension;

import io.avaje.inject.test.InjectJunitExtension;
import jakarta.inject.Inject;
import org.example.coffee.CoffeeMaker;
import org.example.coffee.Pump;
import org.example.coffee.grind.Grinder;
import org.example.missing.MFoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(InjectJunitExtension.class)
class WithExtnBasicTest {

  @Mock Pump pump;

  @Mock Grinder grinder;

  @Spy CoffeeMaker coffeeMaker;

  @Inject MFoo foo;

  @Test
  void test_expect_allTestThingsWired() {
    assertThat(pump).isNotNull();
    assertThat(grinder).isNotNull();
    assertThat(coffeeMaker).isNotNull();
    assertThat(foo).isNotNull();
  }

  @Test
  void testAgain() {
    when(grinder.grindBeans()).thenReturn("stubGrind");

    final String stuff = coffeeMaker.prepare();
    assertThat(stuff).isEqualTo("prepare: stubGrind");
  }
}
