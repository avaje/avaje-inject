package org.example.myapp.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class TestObservesFactory {

  @Inject ObservesFactory.ManagedLifecycle bean;

  @Test
  void factoryObserverReceivesBeanItProvides() {
    assertThat(bean.isStopped()).isFalse();

    bean.fire();

    assertThat(bean.isStopped()).isTrue();
  }
}
