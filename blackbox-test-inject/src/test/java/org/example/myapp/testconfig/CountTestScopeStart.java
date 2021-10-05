package org.example.myapp.testconfig;

import java.util.concurrent.atomic.AtomicInteger;

public class CountTestScopeStart {

  public static AtomicInteger started = new AtomicInteger();
  public static AtomicInteger stopped = new AtomicInteger();

  public void onStart() {
    started.incrementAndGet();
  }

  public void onStop() {
    stopped.incrementAndGet();
    System.out.println("STOPPED !! ");
  }
}
