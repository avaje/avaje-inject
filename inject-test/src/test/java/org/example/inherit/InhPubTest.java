package org.example.inherit;

import io.avaje.inject.xtra.ApplicationScope;
import org.example.inherit.notpublic.PubExposed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InhPubTest {

  @Test
  void test() {
    final InhPub one = ApplicationScope.get(InhPub.class);
    PubExposed exposed = one.exposed();

    assertThat(exposed).isNotNull();
    assertThat(exposed.hello()).isEqualTo("hello");
    assertThat(exposed.ifaceMethod()).isEqualTo("ifaceMethod");
  }
}
