package org.example.myapp;

import jakarta.inject.Singleton;
import org.example.myapp.aspect.MyTimed;

import java.io.IOException;

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
    System.out.println("runOnly "+param);
  }
}
