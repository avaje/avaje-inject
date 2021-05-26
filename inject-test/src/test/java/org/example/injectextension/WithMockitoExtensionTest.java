package org.example.injectextension;

import org.example.missing.MFoo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WithMockitoExtensionTest {

  @Mock
  MFoo foo;

  @Test
  void test() {
    assertThat(foo).isNotNull();
  }
}
