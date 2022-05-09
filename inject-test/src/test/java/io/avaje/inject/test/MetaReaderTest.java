package io.avaje.inject.test;

import org.example.coffee.Pump;
import org.example.coffee.grind.AMusher;
import org.example.coffee.grind.Grinder;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

class MetaReaderTest {

  @Mock Pump pump;

  @Mock Grinder grinder;

  static @Mock AMusher musher;

  @Test
  void checkMetaReader() {
    MetaReader metaReader = new MetaReader(MetaReaderTest.class);
    assertThat(metaReader.hasInstanceInjection()).isTrue();
    assertThat(metaReader.hasClassInjection()).isTrue();
    assertThat(metaReader.mocks).hasSize(2);
    assertThat(metaReader.staticMocks).hasSize(1);
  }
}
