package org.example.generic;

import io.avaje.inject.spi.GenericType;
import org.example.iface.IfaceParam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTypeTest {

  static final Type TYPE_IfaceParamClass = IfaceParam.class;
  static final Type TYPE_IfaceParamNone = new GenericType<IfaceParam>(){};
  static final Type TYPE_IfaceParamWild = new GenericType<IfaceParam<?>>(){};
  static final Type TYPE_IfaceParamGeneric = new GenericType<IfaceParam<Integer>>(){};

  @Test
  void test() {
    assertThat(TYPE_IfaceParamClass.getTypeName()).isEqualTo("org.example.iface.IfaceParam");
    assertThat(TYPE_IfaceParamNone.getTypeName()).isEqualTo("org.example.iface.IfaceParam");
    assertThat(TYPE_IfaceParamWild.getTypeName()).isEqualTo("org.example.iface.IfaceParam<?>");
    assertThat(TYPE_IfaceParamGeneric.getTypeName()).isEqualTo("org.example.iface.IfaceParam<java.lang.Integer>");
  }
}
