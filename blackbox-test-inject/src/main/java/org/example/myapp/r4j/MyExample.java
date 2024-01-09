package org.example.myapp.r4j;

import org.example.myapp.resilience4j.MyRetry;

import io.avaje.inject.Component;
import io.avaje.inject.aop.AOPFallback;

@Component
public class MyExample {

  public int barfCounter;
  public int retryWithFallbackCounter;

  @MyRetry
  public void doingItWithRetry() throws Throwable, IllegalStateException{
    barfCounter++;
    throw new IllegalArgumentException("no");
  }

  @MyRetry(maxAttempts = 5)
  public String retryWithFallback() {
    retryWithFallbackCounter++;
    throw new IllegalArgumentException("no");
  }

  @AOPFallback("retryWithFallback")
  public String myFallback() {
    return "fallback-response";
  }

  @MyRetry
  public String retry2(String param0, int param1) {
    retryWithFallbackCounter++;
    throw new IllegalArgumentException("Retry2Fail[" + param0 + "," + param1 + "]");
  }

  @AOPFallback("retry2")
  public String fallbackRetry2(String param0, int param1, Throwable e) {
    return "fallbackRetry2-" + param0 + ":" + param1 + ":" + e.getMessage();
  }
}
