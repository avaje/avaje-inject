package org.example.myapp.beantypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.example.myapp.lazy.generic.LazyGenericImpl;
import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

import java.util.Calendar;
import java.util.GregorianCalendar;

class BeanTypesTest {

  @Test
  void testBeanTypesRestrictingInjection() {
    try (var scope = BeanScope.builder().build()) {

      assertFalse(scope.contains(BeanTypeComponent.class));
      assertThat(scope.get(AbstractSuperClass.class)).isNotNull();
      assertThat(scope.get(LimitedInterface.class)).isNotNull();
      assertThat(scope.get(CharSequence.class)).isEqualTo("IAmNullable");

      Object extraTypeGregorianCalendar = scope.get(Calendar.class);
      assertThat(extraTypeGregorianCalendar).isNotNull();
      assertThat(extraTypeGregorianCalendar).isInstanceOf(LazyGenericImpl.class);

      Object extraTypeCalendar = scope.get(GregorianCalendar.class);
      assertThat(extraTypeCalendar).isNotNull();
      assertThat(extraTypeCalendar).isInstanceOf(BeanTypeComponent.class);
    }
  }
}
