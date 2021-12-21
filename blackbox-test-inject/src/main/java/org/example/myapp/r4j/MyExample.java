package org.example.myapp.r4j;

import jakarta.inject.Singleton;
import org.example.myapp.resilience4j.MyRetry;

@Singleton
public class MyExample {

  public int barfCounter;
  public int retryWithFallbackCounter;

  @MyRetry
  public void doingItWithRetry() {
    barfCounter++;
    throw new IllegalArgumentException("no");
  }

  @MyRetry(fallbackMethod = "myFallback", maxAttempts = 5)
  public String retryWithFallback() {
    retryWithFallbackCounter++;
    throw new IllegalArgumentException("no");
  }

  public String myFallback() {
    return "fallback-response";
  }

  @MyRetry(fallbackMethod = "fallbackRetry2")
  public String retry2(String param0, int param1) {
    retryWithFallbackCounter++;
    throw new IllegalArgumentException("Retry2Fail[" + param0 + "," + param1 + "]");
  }

  public String fallbackRetry2(String param0, int param1, Throwable e) {
    return "fallbackRetry2-" + param0 + ":" + param1 + ":" + e.getMessage();
  }
}
