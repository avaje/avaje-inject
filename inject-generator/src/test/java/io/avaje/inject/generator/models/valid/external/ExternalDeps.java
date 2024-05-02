package io.avaje.inject.generator.models.valid.external;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.avaje.inject.External;
import io.avaje.lang.Nullable;
import jakarta.inject.Singleton;

@Singleton
public class ExternalDeps {

  public ExternalDeps(@External AtomicBoolean bool, @Nullable AtomicInteger inty) {}
}
