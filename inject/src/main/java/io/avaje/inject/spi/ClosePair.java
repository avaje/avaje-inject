package io.avaje.inject.spi;

public final /*value*/ class ClosePair implements Comparable<ClosePair> {

  private final int priority;
  private final PreDestroyHook closeable;

  public ClosePair(int priority, PreDestroyHook closeable) {
    this.priority = priority;
    this.closeable = closeable;
  }

  public int priority() {
    return priority;
  }

  public PreDestroyHook hook() {
    return closeable;
  }

  @Override
  public int compareTo(ClosePair o) {
    return Integer.compare(priority, o.priority);
  }
}
