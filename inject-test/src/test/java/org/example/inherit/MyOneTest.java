package org.example.inherit;

import io.avaje.inject.test.InjectJunitExtension;
import jakarta.inject.Inject;
import org.example.inherit.notpublic.PubExposed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(InjectJunitExtension.class)
public class MyOneTest extends MyOneAbstract {

  @Inject
  PubExposed something;

  @Test
  void test_expect_inheritedFieldIsInjected() {
    assertThat(something).isNotNull();
    assertThat(steamer).isNotNull();
  }
}
