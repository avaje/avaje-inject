package io.avaje.inject.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

final class FallbackFinder {

  static Fallback find(String name, Method method) throws NoSuchMethodException {
    final Class<?> type = method.getDeclaringClass();
    final Parameter[] parameters = method.getParameters();
    try {
      return new WithThrowable(type.getDeclaredMethod(name, paramTypesWithThrowable(parameters)));
    } catch (NoSuchMethodException e) {
      return new WithoutThrowable(type.getDeclaredMethod(name, paramTypes(parameters)));
    }
  }

  private static Class<?>[] paramTypes(Parameter[] parameters) {
    final Class<?>[] paramTypes = new Class[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      paramTypes[i] = parameters[i].getType();
    }
    return paramTypes;
  }

  private static Class<?>[] paramTypesWithThrowable(Parameter[] parameters) {
    final Class<?>[] paramTypes = new Class[parameters.length + 1];
    for (int i = 0; i < parameters.length; i++) {
      paramTypes[i] = parameters[i].getType();
    }
    paramTypes[parameters.length] = Throwable.class;
    return paramTypes;
  }

  static final class WithThrowable implements Fallback {

    private final Method fallbackMethod;

    WithThrowable(Method fallbackMethod) {
      this.fallbackMethod = fallbackMethod;
    }

    @Override
    public Object invoke(Invocation call, Throwable e) {
      try {
        final Object result = fallbackMethod.invoke(call.instance(), call.arguments(e));
        call.result(result);
        return result;
      } catch (IllegalAccessException | InvocationTargetException ex) {
        throw new InvocationException("Error invoking fallback method", ex);
      }
    }
  }

  static final class WithoutThrowable implements Fallback {

    private final Method fallbackMethod;

    WithoutThrowable(Method fallbackMethod) {
      this.fallbackMethod = fallbackMethod;
    }

    @Override
    public Object invoke(Invocation call, Throwable e) {
      try {
        final Object result = fallbackMethod.invoke(call.instance(), call.arguments());
        call.result(result);
        return result;
      } catch (IllegalAccessException | InvocationTargetException ex) {
        throw new InvocationException("Error invoking fallback method", ex);
      }
    }
  }
}
