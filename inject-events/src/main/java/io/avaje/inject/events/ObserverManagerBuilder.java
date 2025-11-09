package io.avaje.inject.events;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

final class ObserverManagerBuilder implements ObserverManager.Builder {

  private Executor executor = ForkJoinPool.commonPool();

  @Override
  public ObserverManager.Builder asyncExecutor(Executor executor) {
    this.executor = Objects.requireNonNull(executor);
    return this;
  }

  @Override
  public ObserverManager build() {
    return new DObserverManager(executor);
  }
}
