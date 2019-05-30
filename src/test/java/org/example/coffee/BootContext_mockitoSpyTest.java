package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.example.coffee.grind.Grinder;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BootContext_mockitoSpyTest {

  @Test
  public void withBeans_asMocks() {

    Pump pump = mock(Pump.class);
    Grinder grinder = mock(Grinder.class);

    try (BeanContext context = new BootContext()
      .withBeans(pump, grinder)
      .load()) {

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      coffeeMaker.makeIt();

      Pump pump1 = context.getBean(Pump.class);
      Grinder grinder1 = context.getBean(Grinder.class);

      assertThat(pump1).isSameAs(pump);
      assertThat(grinder1).isSameAs(grinder);

      verify(pump).pumpWater();
      verify(grinder).grindBeans();
    }
  }

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

      // or setup here ...
      Pump pump = context.getBean(Pump.class);
      doNothing().when(pump).pumpSteam();

      // act
      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      coffeeMaker.makeIt();

      verify(pump).pumpWater();
      verify(pump).pumpSteam();
    }
  }

  @Test
  public void withMockitoMock_expect_mockUsed() {

    AtomicReference<Grinder> mock = new AtomicReference<>();

    try (BeanContext context = new BootContext()
      .withMock(Pump.class)
      .withMock(Grinder.class, grinder -> {
        // setup the mock
        when(grinder.grindBeans()).thenReturn("stub response");
        mock.set(grinder);
      })
      .load()) {

      Grinder grinder = context.getBean(Grinder.class);
      assertThat(grinder).isSameAs(mock.get());

      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
      assertThat(coffeeMaker).isNotNull();
      coffeeMaker.makeIt();

      verify(grinder).grindBeans();
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
