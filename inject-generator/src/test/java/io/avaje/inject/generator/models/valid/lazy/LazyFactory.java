package io.avaje.inject.generator.models.valid.lazy;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import io.avaje.inject.Primary;

@Lazy
@Factory
public class LazyFactory {

  @Bean
  Integer lazyInt() {
    return 0;
  }

  @Primary
  @Bean
  LazyInterface lazyInterface() {
    return null;
  }
}
