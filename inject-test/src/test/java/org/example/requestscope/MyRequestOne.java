package org.example.requestscope;

import io.avaje.inject.Request;
import javax.inject.Inject;
import org.example.request.AService;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.util.Optional;

@Request
class MyRequestOne implements Closeable {

  /**
   * Wiring a bean scope singleton.
   */
  private final AService service;
  /**
   * Wiring an bean provided to the request scope.
   */
  private final ReqThing reqThing;

  private AService otherService;
  private ReqThing otherReqThing;

  /**
   * Field injection on request scoped bean.
   */
  @Inject
  Optional<AService> myopt;

  private boolean firedPostConstruct;
  private boolean firedMethodInjection;
  private boolean firedClose;

  MyRequestOne(AService service, ReqThing reqThing) {
    this.service = service;
    this.reqThing = reqThing;
  }

  /**
   * Method injection on request scoped bean.
   */
  @Inject
  void foo(AService other, ReqThing otherThing) {
    firedMethodInjection = true;
    otherService = other;
    otherReqThing = otherThing;
  }

  /**
   * Run immediately after field/method injection of request scoped bean.
   */
  @PostConstruct
  void init() {
    firedPostConstruct = true;
  }

  /**
   * Closable automatically treated as PreDestroy for request scoped beans.
   */
  @Override
  public void close() {
    firedClose = true;
  }

  AService getService() {
    return service;
  }

  ReqThing getReqThing() {
    return reqThing;
  }

  AService getOtherService() {
    return otherService;
  }

  ReqThing getOtherReqThing() {
    return otherReqThing;
  }

  boolean isFiredPostConstruct() {
    return firedPostConstruct;
  }

  boolean isFiredMethodInjection() {
    return firedMethodInjection;
  }

  boolean isFiredClose() {
    return firedClose;
  }
}
