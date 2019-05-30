package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.example.coffee.grind.Grinder;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class BootContext_mockitoSpyTest {

  @Test
  public void withMockitoSpy_noSetup_expect_spyUsed() {

    try (BeanContext context = new BootContext()
      .withSpy(Pump.class)
      .load()) {

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      Pump pump = context.getBean(Pump.class);
      verify(pump).pumpWater();
    }
  }

  @Test
  public void withMockitoSpy_postLoadSetup_expect_spyUsed() {

    try (BeanContext context = new BootContext()
      .withSpy(Pump.class)
      .load()) {

      // setup after load()
      Pump pump = context.getBean(Pump.class);
      doNothing().when(pump).pumpWater();

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(pump).pumpWater();
    }
  }

  @Test
  public void withMockitoSpy_expect_spyUsed() {

    try (BeanContext context = new BootContext()
      .withSpy(Pump.class, pump -> {
        // setup the spy
        doNothing().when(pump).pumpWater();
      })
      .load()) {

      Pump pump = context.getBean(Pump.class);

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(pump).pumpWater();
    }
  }

  @Test
  public void withMockitoMock_expect_mockUsed() {

    AtomicReference<Pump> mock = new AtomicReference<>();

    try (BeanContext context = new BootContext()
      .withMock(Grinder.class)
      .withMock(Pump.class, pump -> {
        // do something interesting to setup the mock
        mock.set(pump);
      })
      .load()) {

      Pump pump = context.getBean(Pump.class);
      assertThat(pump).isSameAs(mock.get());

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
    }
  }

  /**
   * Our test double that we want to wire.
   */
  class TDPump implements Pump {

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
