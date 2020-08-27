package org.example.coffee;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import io.avaje.inject.SystemContext;
import org.example.coffee.core.DuperPump;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoffeeMakerTest {

  @Test
  public void makeIt_via_SystemContext() {

    String makeIt = SystemContext.getBean(CoffeeMaker.class).makeIt();
    assertThat(makeIt).isEqualTo("done");

    Pump pump = SystemContext.getBean(Pump.class);
    assertThat(pump).isInstanceOf(DuperPump.class);

    Pump pump2 = SystemContext.context().getBean(Pump.class);
    assertThat(pump2).isSameAs(pump);
  }

  @Test
  public void makeIt_via_BootContext_withNoShutdownHook() {

    try (BeanContext context = new BeanContextBuilder()
      .withNoShutdownHook()
      .build()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

  @Test
  public void makeIt_via_BootContext() {

    try (BeanContext context = new BeanContextBuilder().build()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

}
