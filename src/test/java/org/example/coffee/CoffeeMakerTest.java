package org.example.coffee;

import io.kanuka.BeanContext;
import io.kanuka.BootContext;
import io.kanuka.SystemContext;
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
  }

  @Test
  public void makeIt_via_BootContext_withNoShutdownHook() {

    try (BeanContext context = new BootContext()
      .withNoShutdownHook()
      .load()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }

  @Test
  public void makeIt_via_BootContext() {

    try (BeanContext context = new BootContext().load()) {

      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
      assertThat(makeIt).isEqualTo("done");
    }
  }
//
//  @Test
//  public void makeIt_with_testDoubles() {
//
//    TDPump testDoublePump = new TDPump();
//
//    try (BeanContext context = new BootContext()
//      .addBean(testDoublePump)
//      .load()) {
//
//      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
//      assertThat(makeIt).isEqualTo("done");
//
//      assertThat(testDoublePump.steam).isEqualTo(1);
//      assertThat(testDoublePump.water).isEqualTo(1);
//    }
//  }
//
//  class TDPump implements Pump {
//
//    int water;
//    int steam;
//
//    @Override
//    public void pumpWater() {
//      water++;
//    }
//
//    @Override
//    public void pumpSteam() {
//      steam++;
//    }
//  }
}
