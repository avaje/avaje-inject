package org.example.custom1087;

import jakarta.inject.Singleton;

@ReqScope
@Singleton
public class ReqHandler {

  private final ReqHolder<Req> request;

  public ReqHandler(ReqHolder<Req> request) {
    this.request = request;
  }

  public String handle() {
    return "Hello, " + request.get().name();
  }
}
