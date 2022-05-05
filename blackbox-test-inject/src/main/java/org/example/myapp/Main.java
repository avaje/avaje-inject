package org.example.myapp;

import io.avaje.inject.BeanScope;

public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    String greeting = helloService.hello();
    System.out.println("Greeting: "+greeting);

    assert greeting.equals("hello+AppHelloData");
  }
}
