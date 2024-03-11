package io.avaje.inject.generator.models.valid.lazy;

import io.avaje.inject.Lazy;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Lazy
@Singleton
public class LazyBean {
  @Inject Provider<Integer> intProvider;
}
