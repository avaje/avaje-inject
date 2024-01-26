package org.example.coffee.qualifier.members;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.example.coffee.qualifier.members.TempQualifier.Scale;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class SpyMeterTest {

  private static final String METRIC =
      "@TempQualifier(defaultVal=0, inject={@NestedAnnotation(inject={})}, someOtherString=\"celsi\", value={CELSIUS})";
  private static final String IMPERIAL =
      "@TempQualifier(defaultVal=0, inject={@NestedAnnotation(inject={})}, someOtherString=\"far\", value={FAHRENHEIT})";

  @Spy
  @TempQualifier(value = Scale.FAHRENHEIT, someOtherString = "far")
  Thermometer imperial;

  @Spy
  @TempQualifier(value = Scale.CELSIUS, someOtherString = "celsi")
  Thermometer metric;

  @Inject Meters meters;

  @Test
  void spy_verify() {

    assertThat(meters.imperial.freezing(32)).isTrue();

    verify(imperial).freezing(32);

    assertThat(meters.metric.freezing(32)).isFalse();

    verify(metric).freezing(32);
  }

  @Test
  void test() {

    try (BeanScope beanScope =
        BeanScope.builder()
            .forTesting()
            .spy(Thermometer.class, IMPERIAL)
            .spy(Thermometer.class, METRIC)
            .build()) {

      final var meters = beanScope.get(Meters.class);
      assertThat(meters.imperial.freezing(32)).isTrue();

      final Thermometer imperial = beanScope.get(Thermometer.class, IMPERIAL);
      verify(imperial).freezing(32);

      assertThat(meters.metric.freezing(32)).isFalse();
      final Thermometer metric = beanScope.get(Thermometer.class, METRIC);

      verify(metric).freezing(32);
    }
  }
}
