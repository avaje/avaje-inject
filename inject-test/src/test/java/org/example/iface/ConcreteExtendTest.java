package org.example.iface;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcreteExtendTest {

  @Test
  void obtainViaInterface() {

    try (BeanScope context = BeanScope.newBuilder()
      .build()) {

      final IfaseBase ifaseBase = context.get(IfaseBase.class);
      final IfaceExtend ifaceExtend = context.get(IfaceExtend.class);
      final ConcreteExtend concreteExtend = context.get(ConcreteExtend.class);

      assertSame(concreteExtend, ifaceExtend);
      assertSame(concreteExtend, ifaseBase);
      assertEquals("hello", ifaseBase.hello());
    }
  }
}
