package org.example.myapp;

import io.avaje.inject.Component;
import org.example.external.aspect.MyExternalAspect;

@Component
public class SomeExternalAspectUser {

  @MyExternalAspect
  public String hello() {
    return "hello";
  }
}
