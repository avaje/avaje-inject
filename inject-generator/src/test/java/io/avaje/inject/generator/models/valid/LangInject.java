package io.avaje.inject.generator.models.valid;

import java.lang.annotation.Annotation;

import io.avaje.lang.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LangInject {

  @Inject @Nullable Annotation classLoadingMXBean;
}
