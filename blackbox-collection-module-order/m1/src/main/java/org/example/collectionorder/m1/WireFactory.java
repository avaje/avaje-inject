package org.example.collectionorder.m1;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import java.util.List;
import org.example.collectionorder.api.Contrib;
import org.example.collectionorder.api.Mapper;
import org.example.collectionorder.api.SchemaConfig;

@Factory
public class WireFactory {

  @Bean
  SchemaConfig schemaConfig(Mapper mapper) {
    return new SchemaConfig(mapper);
  }

  @Bean
  Mapper mapper(List<Contrib> contribs) {
    return new Mapper(contribs);
  }
}
