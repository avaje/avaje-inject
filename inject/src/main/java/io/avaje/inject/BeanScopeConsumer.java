package io.avaje.inject;

import java.util.function.Consumer;
/** Consumer interface that takes a completed BeanScope as an argument */
@FunctionalInterface
public interface BeanScopeConsumer extends Consumer<BeanScope> {}
