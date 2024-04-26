package org.example.myapp.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import io.avaje.lang.Nullable;
import jakarta.inject.Named;

@Lazy
@Factory
public class LazyFactory {

  @Bean
  @Named("factory")
  LazyBean lazyInt(@External AtomicBoolean initialized) {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyBean();
  }

  @Bean
  @Named("factoryThrows")
  LazyBean lazyIntThrows(@External AtomicBoolean initialized) throws Exception {
    return lazyInt(initialized);
  }
}
