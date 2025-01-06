package org.example.myapp;

import java.util.concurrent.atomic.AtomicInteger;

public class MyNestedDestroy {

  public static AtomicInteger started = new AtomicInteger();
  public static AtomicInteger stopped = new AtomicInteger();

  public static void reset() {
    started.set(0);
    stopped.set(0);
  }

  public void start() {
    started.incrementAndGet();
  }

  public Reaper reaper() {
    return new Reaper();
  }

  public static class Reaper {

    public void stop() {
      stopped.incrementAndGet();
    }
  }
}
