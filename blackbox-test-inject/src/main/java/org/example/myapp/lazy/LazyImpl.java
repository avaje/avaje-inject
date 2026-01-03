package org.example.myapp.lazy;

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
@BeanTypes(LazyInterface.class)
public class LazyImpl implements LazyInterface {

  private AtomicBoolean initialized;
  private AtomicBoolean destroyed = new AtomicBoolean(false);

  public LazyImpl(@Nullable AtomicBoolean initialized) {
    this.initialized = initialized;
  }

  @PostConstruct
  void init(BeanScope scope) {
    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
  }

  void shutdown() {
    destroyed.set(true);
  }

  @Override
  public boolean isDestroyed() {
    return destroyed.get();
  }

  @Override
  public void something() {}

  @Override
  public void otherThing() {}
}
