package org.example.coffee.qualifier.members;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.coffee.qualifier.members.TempQualifier.Scale;

@InjectTest
public class QualifiedMemberTest {

  @Inject Meters meters;

  @Test
  public void test() {

    assertThat(meters.imperial).isInstanceOf(ImperialMeter.class);
    assertThat(meters.metric).isInstanceOf(MetricMeter.class);
  }
}
