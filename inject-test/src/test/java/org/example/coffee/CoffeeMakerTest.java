package org.example.coffee;

import io.avaje.inject.ApplicationScope;
import io.avaje.inject.BeanScope;
import org.example.coffee.core.DuperPump;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoffeeMakerTest {

  @Test
  public void makeIt_via_SystemContext() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");

      Pump pump = context.get(Pump.class);
      assertThat(pump).isInstanceOf(DuperPump.class);
    }
  }

  @Test
  public void makeIt_via_BootContext_withNoShutdownHook() {

    try (BeanScope context = BeanScope.newBuilder()
      .withShutdownHook(false)
      .build()) {

      String makeIt = context.get(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

  @Test
  public void makeIt_via_BootContext() {

    try (BeanScope context = BeanScope.newBuilder().build()) {
      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

}
