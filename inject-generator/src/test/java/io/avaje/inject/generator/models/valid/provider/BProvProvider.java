package io.avaje.inject.generator.models.valid.provider;

import io.avaje.inject.Component;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BProvProvider implements Provider<BProv<String>> {

  AtomicInteger counter = new AtomicInteger();

  @Override
  public BProv<String> get() {
    return new BProv<>("Hello BProv" + counter.incrementAndGet());
  }
}
