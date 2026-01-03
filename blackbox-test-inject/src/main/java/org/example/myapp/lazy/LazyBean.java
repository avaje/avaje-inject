package org.example.myapp.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Lazy;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Lazy
@Singleton
@Named("single")
public class LazyBean {

  @Inject @Nullable AtomicBoolean initialized;
  private AtomicBoolean destroyed = new AtomicBoolean(false);

  @PostConstruct
  void init(BeanScope scope) {
    // note that nested test scopes will not be lazy
    if (initialized != null) initialized.set(true);
  }

  @PreDestroy
  void shutdown() {
    destroyed.set(true);
  }

  boolean isDestroyed() {
	return destroyed.get();
  }

  void something() {}

  public void other() {}

  protected void other2() {}
}
