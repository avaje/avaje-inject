package org.example.myapp.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;
import io.avaje.lang.Nullable;
import jakarta.inject.Named;

@Lazy
@Factory
public class LazyFactory {

  @Bean
  @Named("factory")
  LazyBean lazyInt(@Nullable AtomicBoolean initialized) {

    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
    return new LazyBean();
  }
}
