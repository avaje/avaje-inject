package org.example.myapp.lazy2;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.myapp.HelloService;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class LazyOneB {

  public static final AtomicBoolean BINIT = new AtomicBoolean();

  final HelloService helloService;

  @Inject
  LazyOneB(HelloService helloService) {
    this.helloService = helloService; // non-lazy dependency
    BINIT.set(true);
  }

  /** Required by Lazy proxy */
  LazyOneB() {
    this.helloService = null;
  }

  public String oneB() {
    return "oneB";
  }

  public HelloService helloService() {
    return helloService;
  }
}
