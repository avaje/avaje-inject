package org.example.coffee;

import io.kanuka.BeanContext;
import io.kanuka.BootContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BootContextAddTest {


  @Test
  public void withBean_expect_testDoublePumpUsed() {

//    TDPump testDoublePump = new TDPump();
//
//    try (BeanContext context = new BootContext()
//      .withBean(testDoublePump)
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
