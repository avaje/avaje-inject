package org.example.coffee.prototype;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyProtoTest {

  @Test
  void test_prototype_differentInstance() {
    try (BeanScope scope = BeanScope.newBuilder()
      .build()) {

      MyProto one = scope.get(MyProto.class);
      MyProto two = scope.get(MyProto.class);
      assertThat(one).isNotSameAs(two);
      // singleton dependency is same instance
      assertThat(one.pump()).isSameAs(two.pump());

      OtherProto otherOne = scope.get(OtherProto.class);
      OtherProto otherTwo = scope.get(OtherProto.class);

      assertThat(otherOne).isNotSameAs(otherTwo);
      assertThat(otherOne.myProto).isNotSameAs(otherTwo.myProto);
    }
  }

}
