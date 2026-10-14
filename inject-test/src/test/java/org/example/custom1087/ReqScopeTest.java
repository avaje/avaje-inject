package org.example.custom1087;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReqScopeTest {

  @Test
  void genericRequiresString() {
    ReqHolder<Req> holder = () -> new Req("rob");
    try (BeanScope scope = BeanScope.builder()
      .modules(new ReqModule(holder))
      .build()) {

      assertThat(scope.get(ReqHandler.class).handle()).isEqualTo("Hello, rob");
    }
  }
}
