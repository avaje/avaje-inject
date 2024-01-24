package org.example.myapp.assist.droid;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DroidThingTest {

  @Test
  void assistFactoryInjectionTypes() {
    try (BeanScope testScope = BeanScope.builder().build()) {
      DroidThing droidThing = testScope.get(DroidThing.class);

      assertThat(droidThing).isNotNull();

      DroidFactory.Droid fDroid = droidThing.viaField(1, Model.BATTLE);
      assertThat(fDroid).isInstanceOf(DroidField.class);
      assertThat(fDroid.dependenciesAreWired()).isTrue();

      DroidFactory.Droid cDroid = droidThing.viaCtor(1, Model.BATTLE);
      assertThat(cDroid).isInstanceOf(DroidConstructor.class);
      assertThat(cDroid.dependenciesAreWired()).isTrue();

      DroidFactory.Droid mDroid = droidThing.viaMethod(1, Model.BATTLE);
      assertThat(mDroid).isInstanceOf(DroidMethod.class);
      assertThat(mDroid.dependenciesAreWired()).isTrue();
    }
  }
}
