package org.example.coffee.prototype;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import io.avaje.inject.Prototype;
import jakarta.inject.Inject;

import java.io.Closeable;
import java.io.IOException;

@Prototype
public class ExtraProto implements Closeable {

  boolean initRun;
  boolean destroyed;

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

  @PreDestroy
  void destroy() {
    destroyed = true;
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

  public boolean destroyed() {
    return destroyed;
  }

  @Override
  public void close() throws IOException {

  }
}
