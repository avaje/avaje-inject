package org.example.coffee.prototype;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.Prototype;
import javax.inject.Inject;

import java.io.Closeable;
import java.io.IOException;

@Prototype
public class ExtraProto implements Closeable {

  boolean initRun;

  @Inject
  MyProto fieldInjected;

  OtherProto methodInjected;

  @Inject
  void methodInjected(OtherProto methodInjected) {
    this.methodInjected = methodInjected;
  }

  @PostConstruct
  void init() {
    initRun = true;
  }

  public MyProto fieldInjected() {
    return fieldInjected;
  }

  public OtherProto methodInjected() {
    return methodInjected;
  }

  public boolean initRun() {
    return initRun;
  }

  @Override
  public void close() throws IOException {

  }

  // Compilation error expected with PreDestroy
  // @PreDestroy
  // void foo() { }
}
