package io.avaje.inject.test;

import io.avaje.inject.BeanScopeBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class SetupMethods {

  private final List<Method> staticMethods = new ArrayList<>();
  private final List<Method> instanceMethods = new ArrayList<>();

  SetupMethods(Class<?> testClass) {
    var analyzedClass = testClass;
    var hierarchy = new LinkedList<Class<?>>();
    while (analyzedClass != null && !analyzedClass.equals(Object.class)) {
      hierarchy.addFirst(analyzedClass);
      analyzedClass = analyzedClass.getSuperclass();
    }
    for (Class<?> aClass : hierarchy) {
      for (Method method : aClass.getDeclaredMethods()) {
        if (method.getDeclaredAnnotation(Setup.class) != null) {
          // should be our method
          if (Modifier.isStatic(method.getModifiers())) {
            method.setAccessible(true);
            staticMethods.add(method);
          } else {
            method.setAccessible(true);
            instanceMethods.add(method);
          }
        }
      }
    }
  }

  boolean hasStaticMethods() {
    return !staticMethods.isEmpty();
  }

  boolean hasInstanceMethods() {
    return !instanceMethods.isEmpty();
  }

  void invokeStatics(BeanScopeBuilder builder) {
    try {
      for (Method method : staticMethods) {
        method.invoke(null, builder);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException("Expecting method with single argument of BeanScopeBuilder but ... ", e);
    }
  }

  void invokeInstance(BeanScopeBuilder builder, Object testInstance) {
    try {
      for (Method method : instanceMethods) {
        method.invoke(testInstance, builder);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException("Expecting method with single argument of BeanScopeBuilder but ... ", e);
    }
  }

}
