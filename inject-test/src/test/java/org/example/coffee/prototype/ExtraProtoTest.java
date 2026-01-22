package org.example.coffee.prototype;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraProtoTest {

  @Test
  void fieldMethodInjection() {
    ExtraProto extra;
    try (BeanScope scope = BeanScope.builder().build()) {

      extra = scope.get(ExtraProto.class);

      assertThat(extra.fieldInjected()).isNotNull();
      assertThat(extra.methodInjected()).isNotNull();
      assertThat(extra.initRun()).isTrue();
      assertThat(extra.destroyed()).isFalse();
    }
    assertThat(extra.destroyed()).isTrue();
  }
}
