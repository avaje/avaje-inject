package io.avaje.inject.generator.models.valid;

import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.generator.models.valid.C_C.Rika;

@Factory
public class ListFactory {

  @Bean(initMethod = "postConstruct")
  List<Something> userId(List<Short> shortyList) {
    return List.of(new Something());
  }

  @Bean
  Rika curseRika() {
    return null;
  }

  public void close() throws Exception {}

  public static class Something implements AutoCloseable {

    public void postConstruct() {}

    @Override
    public void close() throws Exception {}
  }
}
