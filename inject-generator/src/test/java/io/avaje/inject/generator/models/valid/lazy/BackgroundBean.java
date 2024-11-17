package io.avaje.inject.generator.models.valid.lazy;

import io.avaje.inject.AsyncBean;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
@AsyncBean
public class BackgroundBean {
  @Inject Provider<Integer> intProvider;
}
