package io.avaje.inject;

import java.util.function.Consumer;

@FunctionalInterface
public interface BeanScopeConsumer extends Consumer<BeanScope> {}
