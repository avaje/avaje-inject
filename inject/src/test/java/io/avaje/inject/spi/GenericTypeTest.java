package io.avaje.inject.spi;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericTypeTest {

  @Test
  void testNoSubclass() {
    assertThrows(IllegalArgumentException.class, GenericType::new);
  }

  @Test
  void testNoTypeArguments() {
    assertThrows(IllegalArgumentException.class, NoTypeArgs::new);
  }

  @Test
  void getTypeName() {
    Type type2 = new GenericType<List<String>>() {};
    assertThat(type2.getTypeName()).isEqualTo("java.util.List<java.lang.String>");
  }

  @Test
  void typeArguments() {
    GenericType<String> type = new TypeArgs();

    assertThat(type.type()).isEqualTo(String.class);
  }

  @Test
  void genericTypeList() {
    GenericType<Set<List<String>>> type = new GenericType<Set<List<String>>>() {};

    Type reflectType = type.type();
    assertThat(reflectType).isInstanceOf(ParameterizedType.class);

    ParameterizedType pType = (ParameterizedType) reflectType;

    assertThat(pType.getRawType()).isEqualTo(Set.class);

    Type secondType = pType.getActualTypeArguments()[0];
    assertThat(secondType).isInstanceOf(ParameterizedType.class);
    pType = (ParameterizedType) secondType;
    assertThat(pType.getRawType()).isEqualTo(List.class);
    assertThat(pType.getActualTypeArguments()[0]).isEqualTo(String.class);
  }

  private static class TypeArgs extends GenericType<String> {

  }

  private static class NoTypeArgs<T> extends GenericType<T> {

  }
}
