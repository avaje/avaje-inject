package org.example.inherit;

import io.avaje.inject.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InhOneTest {

  @Test
  void test() {

    final InhOne one = ApplicationScope.get(InhOne.class);

    assertThat(one.expectSetTopField()).isNotNull();
    assertThat(one.expectSetBaseField()).isNotNull();
    assertThat(one.expectSetBaseBaseField()).isNotNull();

    assertThat(one.expectTrueTopMethodCalled()).isTrue();
    assertThat(one.expectTrueBaseMethodCalled()).isTrue();
    assertThat(one.expectTrueBaseBaseNoOverrideCalled()).isTrue();

    assertThat(one.expectTrueNoInjectOnOverride()).isFalse();

    assertThat(one.expectFalseBaseBaseOverride()).isFalse();
    assertThat(one.expectFalseBaseMethodSetOnBaseBase()).isFalse();

    assertThat(one.getBaseMethod()).isNotNull();
    assertThat(one.getTopMethod()).isNotNull();
  }
}
