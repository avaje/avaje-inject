package org.example.initialisation;

import io.avaje.inject.ApplicationInitialisation;
import io.avaje.inject.ApplicationScope;
import org.example.coffee.CoffeeMaker;
import org.example.coffee.Pump;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

class ApplicationInitialisationTest {

  /**
   * Hmmm, we can only run this test by itself as it messes with ApplicationScope.
   */
  @Disabled
  @Test
  void register() {

    // our externally provided dependency
    Pump externalPump = Mockito.mock(Pump.class);

    // register our callback BEFORE ApplicationScope is used
    ApplicationInitialisation.register(appScope -> appScope.withBean(Pump.class, externalPump));

    final Pump pump = ApplicationScope.get(Pump.class);
    assertThat(pump).isSameAs(externalPump);

    CoffeeMaker coffeeMaker = ApplicationScope.get(CoffeeMaker.class);
    assertThat(coffeeMaker).isNotNull();
    coffeeMaker.makeIt();

    verify(pump).pumpSteam();
    verify(pump).pumpWater();
  }

  @Test
  void register_whenAfterInit_expect_exception() {

    // initialising ApplicationScope before register
    ApplicationScope.scope();

    Pump externalPump = Mockito.mock(Pump.class);
    assertThatThrownBy(() -> {
      ApplicationInitialisation.register(appScope -> appScope.withBean(Pump.class, externalPump));
    }).isInstanceOf(IllegalStateException.class)
      .hasMessage("register() being called after ApplicationScope has been initialised");
  }
}
