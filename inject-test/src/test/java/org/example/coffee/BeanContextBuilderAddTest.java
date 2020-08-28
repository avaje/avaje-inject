package org.example.coffee;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

public class BeanContextBuilderAddTest {

  @Test
  public void withModules_exludingThisOne() {
    assertThrows(IllegalStateException.class, ()-> {
      TDPump testDoublePump = new TDPump();

      try (BeanContext context = new BeanContextBuilder()
        .withBeans(testDoublePump)
        // our module is "org.example.coffee"
        // so this effectively includes no modules
        .withModules("other")
        .build()) {

        CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
        assertThat(coffeeMaker).isNull();
      }
    });
  }


  @Test
  public void withModules_includeThisOne() {

    TDPump testDoublePump = new TDPump();

    try (BeanContext context = new BeanContextBuilder()
      .withBeans(testDoublePump)
      .withModules("org.example")
      .build()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      assertThat(testDoublePump.steam).isEqualTo(1);
      assertThat(testDoublePump.water).isEqualTo(1);
    }
  }

  @Test
  public void withBean_expect_testDoublePumpUsed() {

    TDPump testDoublePump = new TDPump();

    try (BeanContext context = new BeanContextBuilder()
      .withBeans(testDoublePump)
      .build()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      assertThat(testDoublePump.steam).isEqualTo(1);
      assertThat(testDoublePump.water).isEqualTo(1);
    }
  }

  @Test
  public void withMockitoMock_expect_mockUsed() {

    Pump mock = Mockito.mock(Pump.class);

    try (BeanContext context = new BeanContextBuilder()
      .withBean(Pump.class, mock)
      .build()) {

      Pump pump = context.getBean(Pump.class);
      assertThat(pump).isSameAs(mock);

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(pump).pumpSteam();
      verify(pump).pumpWater();
    }
  }

  /**
   * Our test double that we want to wire.
   */
  static class TDPump implements Pump {

    int water;
    int steam;

    @Override
    public void pumpWater() {
      water++;
    }

    @Override
    public void pumpSteam() {
      steam++;
    }
  }
}
