package org.example.myapp.lazy.generic;

import java.util.concurrent.atomic.AtomicBoolean;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Lazy;
import io.avaje.inject.PostConstruct;
import io.github.resilience4j.core.lang.Nullable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Lazy
@Singleton
@Named("single")
@BeanTypes(LazyGenericInterface.class)
public class LazyGenericImpl implements LazyGenericInterface<String> {

  AtomicBoolean initialized;

  public LazyGenericImpl(@Nullable AtomicBoolean initialized) {
    this.initialized = initialized;
  }

  @PostConstruct
  void init(BeanScope scope) {
    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
  }

  @Override
  public void something() {}

  @Override
  public void otherThing() {}
}
