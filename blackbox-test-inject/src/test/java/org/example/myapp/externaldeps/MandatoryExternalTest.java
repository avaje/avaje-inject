package org.example.myapp.externaldeps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.example.myapp.externaldeps.MandatoryExternalFactory.UsesExternal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;

class MandatoryExternalTest {

  @AfterEach
  void disable() {
    Config.eventBuilder("").remove("externaldeps.factory").remove("externaldeps.observer").publish();
  }

  @Test
  void notEnabled_beansNotWired() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.getOptional(UsesExternal.class)).isEmpty();
      assertThat(scope.getOptional(ObserverExternal.class)).isEmpty();
    }
  }

  @Test
  void methodLevelExternal_supplied() {
    Config.setProperty("externaldeps.factory", "on");
    MandatoryExternal external = new MandatoryExternal() {};
    try (BeanScope scope = BeanScope.builder().bean(MandatoryExternal.class, external).build()) {
      assertThat(scope.get(UsesExternal.class).external()).isSameAs(external);
    }
  }

  @Test
  void methodLevelExternal_notSupplied_failsAtStartup() {
    Config.setProperty("externaldeps.factory", "on");
    assertThatThrownBy(() -> BeanScope.builder().build()).isInstanceOf(RuntimeException.class);
  }

  @Test
  void observerParamExternal_supplied() {
    Config.setProperty("externaldeps.observer", "on");
    ObserverExternalDep external = new ObserverExternalDep() {};
    try (BeanScope scope = BeanScope.builder().bean(ObserverExternalDep.class, external).build()) {
      var observer = scope.get(ObserverExternal.class);
      observer.fire();
      assertThat(observer.received()).isSameAs(external);
    }
  }
}
