package org.example.myapp.async;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.avaje.inject.AsyncBean;
import io.avaje.lang.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@AsyncBean
@Singleton
@Named("single")
public class BackgroundBean {

  final Instant initTime;
  final String threadName = Thread.currentThread().getName();

  public BackgroundBean(@Nullable AtomicInteger intyAtomic) throws InterruptedException {
    this.initTime = Instant.now();

    if (intyAtomic != null) {
      intyAtomic.incrementAndGet();
    }

    Thread.sleep(2000);
  }
}
