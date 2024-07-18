package io.avaje.inject.generator.models.valid.external;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.External;
import jakarta.inject.Singleton;

@Singleton
public class ExternalDeps {

  @Nullable AtomicLong longyAtomicInteger;

  public ExternalDeps(@External AtomicBoolean bool, @Nullable AtomicInteger inty) {}
}
