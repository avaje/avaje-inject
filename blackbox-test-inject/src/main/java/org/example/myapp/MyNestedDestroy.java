package org.example.myapp;

import java.util.concurrent.atomic.AtomicInteger;

public class MyNestedDestroy {

  public static AtomicInteger STARTED = new AtomicInteger();
  public static AtomicInteger STOPPED = new AtomicInteger();

  public static void reset() {
    STARTED.set(0);
    STOPPED.set(0);
  }

  public void start() {
    STARTED.incrementAndGet();
  }

  public Reaper reaper() {
    return new Reaper();
  }

  public static class Reaper {

    public void stop() {
      STOPPED.incrementAndGet();
    }
  }
}
