package io.avaje.inject;

import java.util.function.Consumer;
/** Consumer interface that takes a fully wired BeanScope as an argument */
@FunctionalInterface
public interface BeanScopeConsumer extends Consumer<BeanScope> {}
