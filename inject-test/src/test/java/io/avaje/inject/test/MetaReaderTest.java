package io.avaje.inject.test;

import org.example.coffee.Pump;
import org.example.coffee.grind.Grinder;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

class MetaReaderTest {

  @Mock
  Pump pump;

  @Mock
  Grinder grinder;

  @Test
  void checkMetaReader() {
    MetaReader metaReader = new MetaReader(this);
    assertThat(metaReader.mocks()).hasSize(2);
  }
}
