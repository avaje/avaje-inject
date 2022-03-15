package org.example.coffee.prototype;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UseProtoTest {

  @Test
  void test_injectProvider() {
    try (BeanScope scope = BeanScope.newBuilder()
      .build()) {
      UseProto useProto = scope.get(UseProto.class);

      MyProto one = useProto.myProto();
      MyProto two = useProto.myProto();
      assertThat(one).isNotSameAs(two);
      // singleton dependency is same instance
      assertThat(one.pump()).isSameAs(two.pump());


      UseProto2 useProto2 = scope.get(UseProto2.class);
      assertThat(useProto2.myProto()).isNotSameAs(two);
      assertThat(useProto2.otherProto()).isNotSameAs(useProto.otherProto());

    }
  }

}
