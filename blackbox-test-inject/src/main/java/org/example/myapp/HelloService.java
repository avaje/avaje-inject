package org.example.myapp;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyAround;
import org.example.myapp.aspect.MyBefore;

@Singleton
public class HelloService {

  private final HelloData data;

  HelloService(HelloData data) {
    this.data = data;
  }

  public String hello() {
    return "hello+" + data.helloData();
  }

  @MyBefore
  public String foo(String param) {
    return "foo+" + param;
  }

  @MyAround(name="what")
  public String bazz(String param0, int param1) {
    System.out.println("execute bazz ...");
    return "bazz " + param0 + " " + param1;
  }

  @PostConstruct
  void postCon() {

  }

  @PreDestroy
  void preDest() {

  }
}
