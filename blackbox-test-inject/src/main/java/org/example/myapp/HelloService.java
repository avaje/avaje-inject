package org.example.myapp;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import javax.inject.Singleton;
import org.example.myapp.aspect.*;

import java.io.IOException;
import java.io.UncheckedIOException;

@Singleton
public class HelloService {

  private final HelloData data;

  private int counter;
  private String justRunResult;

  HelloService(HelloData data) {
    this.data = data;
  }

  public String hello() {
    return "hello+" + data.helloData();
  }

  @MySkip
  public String skipExample(String p0) {
    // this is never called due to MySkip aspect
    return p0;
  }

  @MyMultiInvoke
  int counter() {
    return counter++;
  }

  @MyBefore
  public String foo(String param, int overloaded) {
    return "foo+" + param;
  }

  @MyBefore
  public String foo(String param) {
    return "foo+" + param;
  }

  @MyAround(name="what")
  public String bazz(String param0, int param1) throws IOException {
    System.out.println("execute bazz ...");
    return "bazz " + param0 + " " + param1;
  }

  @MyAround
  public void justRun(String param0, int param1, int param2) throws IOException, ClassNotFoundException, UncheckedIOException {
    System.out.println("justRun ...");
    justRunResult = param0+" "+param1+" "+param2;
  }

  @MyThrowing
  public void thisWillThrow() {
    System.out.println("this just never gets called");
    throw new IllegalCallerException("never happens");
  }

  @MyAround
  public void appCodeThrowsUnchecked() {
    throw new IllegalArgumentException("appCodeUnchecked");
  }

  /**
   * Declared exception is NOT wrapped in InvocationException
   */
  @MyAround
  public void appCodeThrowsDeclared() throws IllegalArgumentException {
    throw new IllegalArgumentException("appCodeDeclared");
  }

  public void multiAspectsHere() {

  }


  public String justRunResult() {
    return justRunResult;
  }

  @PostConstruct
  void postCon() {

  }

  @PreDestroy
  void preDest() {

  }
}
