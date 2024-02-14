package org.example.coffee.qualifier.members;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class QualifiedMemberTest {

  @Inject
  Meters meters;

  @Test
  void test() {
    assertThat(meters.imperial).isInstanceOf(ImperialMeter.class);
    assertThat(meters.metric).isInstanceOf(MetricMeter.class);
  }
}
