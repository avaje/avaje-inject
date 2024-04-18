package org.example.myapp;

import io.avaje.inject.Component;
import io.avaje.inject.InjectModule;

import org.example.external.aspect.MyExternalAspect;

import io.avaje.inject.BeanScope;
import io.avaje.inject.aop.Aspect.Import;
import org.example.myapp.other.SimulateExternal;
import org.example.myapp.other.SimulateExternal2;
import org.example.myapp.other.SimulateExternalPub;
import org.example.myapp.other.SimulateExternalPub2;

@Component.Import(value = {SimulateExternal.class, SimulateExternal2.class, SimulateExternalPub.class, SimulateExternalPub2.class}) //, packagePrivate = true)
@Import(MyExternalAspect.class)
public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    String greeting = helloService.hello();
    System.out.println("Greeting: " + greeting);

    assert "hello+AppHelloData".equals(greeting);
  }
}
