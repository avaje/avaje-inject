package org.example.coffee;

import io.kanuka.BootContext;
import io.kanuka.SystemContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoffeeMakerTest {

  @Test
  public void makeIt_via_SystemContext() {

    String makeIt = SystemContext.getBean(CoffeeMaker.class).makeIt();

    assertThat(makeIt).isEqualTo("done");
  }

  @Test
  public void makeIt_via_BootContext() {

    String makeIt = new BootContext()
      .load()
      .getBean(CoffeeMaker.class)
      .makeIt();

    assertThat(makeIt).isEqualTo("done");
  }
}
