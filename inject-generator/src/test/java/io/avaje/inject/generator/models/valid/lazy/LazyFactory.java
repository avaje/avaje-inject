package io.avaje.inject.generator.models.valid.lazy;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;

@Lazy
@Factory
public class LazyFactory {

  @Bean
  Integer lazyInt() {
    return 0;
  }
}
