package org.example.coffee.qualifier.members;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class SpyMeterTest {

  private static final String METRIC =
      Integer.getInteger("java.specification.version") > 11
          ? "tempqualifier(celsius)"
          : "tempqualifier(tempqualifier.scale.celsius)";
  private static final String IMPERIAL =
      Integer.getInteger("java.specification.version") > 11
          ? "tempqualifier(fahrenheit)"
          : "tempqualifier(tempqualifier.scale.fahrenheit)";

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
