package io.avaje.inject.spi;

public final class ClosePair implements Comparable<ClosePair> {

  private final int priority;
  private final AutoCloseable closeable;

  public ClosePair(int priority, AutoCloseable closeable) {
    this.priority = priority;
    this.closeable = closeable;
  }

  public int priority() {
    return priority;
  }

  public AutoCloseable closeable() {
    return closeable;
  }

  @Override
  public int compareTo(ClosePair o) {
    return Integer.compare(priority, o.priority);
  }
}
