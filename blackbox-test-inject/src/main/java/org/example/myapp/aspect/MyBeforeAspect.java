package org.example.myapp.aspect;

import io.avaje.inject.Aspect;
import jakarta.inject.Singleton;

import java.lang.reflect.Method;

@Singleton
//@Aspect(target=MyBefore.class)
public class MyBeforeAspect {

  @Aspect.Before
  public void beforeInvoke(Method method) {
    if (method.getName().equals("foo")) {
      throw new IllegalStateException("Can't call this method");
    }
  }
}
