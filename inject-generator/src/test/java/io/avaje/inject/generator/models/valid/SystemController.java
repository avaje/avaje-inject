package io.avaje.inject.generator.models.valid;

import java.lang.management.ClassLoadingMXBean;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SystemController {

  @Inject ClassLoadingMXBean classLoadingMXBean;
}
