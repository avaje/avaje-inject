package org.example.injectextension;

import org.example.coffee.CoffeeMaker;
import org.example.coffee.Pump;
import org.example.coffee.grind.Grinder;
import org.example.missing.MFoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WithMockitoExtensionTest {

  @Mock
  MFoo foo;

  @Test
  void test() {
    assertThat(foo).isNotNull();
  }

  static class ProgTest {
    @Test
    void mockitoStyle() {
      final Pump pump = mock(Pump.class);
      final Grinder grinder = mock(Grinder.class);
      CoffeeMaker coffeeMaker = new CoffeeMaker(pump, grinder);

      // act
      coffeeMaker.makeIt();

      verify(pump).pumpSteam();
      verify(grinder).grindBeans();
    }
  }

  @ExtendWith(MockitoExtension.class)
  static class ExtTest {

    @Mock Pump pump;
    @Mock Grinder grinder;

    @Test
    void mockitoStyle() {
      CoffeeMaker coffeeMaker = new CoffeeMaker(pump, grinder);

      // act
      coffeeMaker.makeIt();

      verify(pump).pumpSteam();
      verify(grinder).grindBeans();
    }
  }
}
