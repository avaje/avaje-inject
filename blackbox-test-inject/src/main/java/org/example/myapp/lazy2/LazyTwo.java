package org.example.myapp.lazy2;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class LazyTwo {

  public static final AtomicBoolean INIT = new AtomicBoolean();

  private final LazyOneB oneB;
  final LazyOneA oneA;

  @Inject
  LazyTwo(LazyOneA oneA, LazyOneB oneB) {
    this.oneA = oneA;
    this.oneB = oneB;
    INIT.set(true);
  }

  /** Required by Lazy proxy */
  LazyTwo() {
    this.oneA = null;
    this.oneB = null;
  }

  String something() {
    return "two-" + oneA.oneA() + "-" + oneB.oneB();
  }

  String description() {
    return this.getClass() + "|" + oneA.getClass() + "|" + oneB.getClass();
  }

  public LazyOneA oneA() {
    return oneA;
  }

  public LazyOneB oneB() {
    return oneB;
  }
}
