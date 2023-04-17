package org.example.myapp;

import org.example.external.aspect.MyExternalAspect;

import io.avaje.inject.BeanScope;
import io.avaje.inject.aop.Aspect.Import;

@Import(MyExternalAspect.class)
public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    String greeting = helloService.hello();
    System.out.println("Greeting: " + greeting);

    assert greeting.equals("hello+AppHelloData");
  }
}
