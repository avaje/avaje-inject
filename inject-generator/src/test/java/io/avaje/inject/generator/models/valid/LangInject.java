package io.avaje.inject.generator.models.valid;

import java.lang.annotation.Annotation;

import io.avaje.inject.External;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LangInject {

  @Inject @External Annotation classLoadingMXBean;
}
