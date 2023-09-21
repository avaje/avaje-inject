package org.example.myapp;

import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyTimed;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@MyTimed
@Singleton
public class ExampleService {

  final HelloService helloService;

  public ExampleService(HelloService helloService) {
    this.helloService = helloService;
  }

  public String other(String param0, int param1) {//} throws IOException, IllegalStateException {
    return "other " + param0 + " " + param1;
  }

  public void runOnly(String param) {
    System.out.println("runOnly " + param);
  }

  public void withParamAtomic(AtomicLong atomicLong) {
    System.out.println("withParamAtomic " + atomicLong);
  }

  public void withParamImport(ConcurrentHashMap<String, String> param0) {
    System.out.println("withParamAtomic " + param0);
  }

  public void withListString(List<String> param0) {
    System.out.println("withListString " + param0);
  }

  @PreDestroy(priority = 1001)
  public void close() {
    MyDestroyOrder.add("ExampleService");
  }
}
