package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BootContextAddTest {

  @Test
  public void withModules_exludingThisOne() {

//    TDPump testDoublePump = new TDPump();
//
//    try (BeanContext context = new BootContext()
//      .withBeans(testDoublePump)
//      // our module is "org.example.coffee"
//      // so this effectively includes no modules
//      .withModules("other")
//      .load()) {
//
//      CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
//      assertThat(coffeeMaker).isNull();
//    }
  }


  @Test
  public void withModules_includeThisOne() {

//    TDPump testDoublePump = new TDPump();
//
//    try (BeanContext context = new BootContext()
//      .withBeans(testDoublePump)
//      .withModules("org.example.coffee")
//      .load()) {
//
//      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
//      assertThat(makeIt).isEqualTo("done");
//
//      assertThat(testDoublePump.steam).isEqualTo(1);
//      assertThat(testDoublePump.water).isEqualTo(1);
//    }
  }

  @Test
  public void withBean_expect_testDoublePumpUsed() {

//    TDPump testDoublePump = new TDPump();
//
//    try (BeanContext context = new BootContext()
//      .withBeans(testDoublePump)
//      .load()) {
//
//      String makeIt = context.getBean(CoffeeMaker.class).makeIt();
//      assertThat(makeIt).isEqualTo("done");
//
//      assertThat(testDoublePump.steam).isEqualTo(1);
//      assertThat(testDoublePump.water).isEqualTo(1);
//    }
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
