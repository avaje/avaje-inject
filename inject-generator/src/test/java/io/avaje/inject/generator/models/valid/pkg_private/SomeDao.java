package io.avaje.inject.generator.models.valid.pkg_private;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.generator.models.valid.pkg_private.SomeDao.SomeMapper;

interface SomeDao {

  String findById(String id);

  interface SomeMapper {

    SomeDao someDao(String keyspace);
  }
}

@Factory
final class SomeUnrelatedExampleFactory {

  @Bean
  SomeMapper mapper() {
    return null;
  }
}
