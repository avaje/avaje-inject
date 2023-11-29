package io.avaje.inject.spi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Cut down version of GenericTypeUtil from Helidon project Apache 2 license.
 */
final class GenericTypeUtil {

  /**
   * Return the Type for type parameter of {@code GenericType<T>}.
   */
  static Type typeArgument(Class<?> clazz) {
    // collect superclasses
    Deque<Type> superclasses = new ArrayDeque<>();
    Type currentType;
    Class<?> currentClass = clazz;
    do {
      currentType = currentClass.getGenericSuperclass();
      superclasses.push(currentType);
      if (currentType instanceof Class) {
        currentClass = (Class<?>) currentType;
      } else if (currentType instanceof ParameterizedType) {
        currentClass = (Class<?>) ((ParameterizedType) currentType).getRawType();
      }
      if (currentClass.equals(Object.class) || currentClass.equals(GenericType.class)) {
        break;
      }
    } while (true);

    // find which one supplies type argument and return it
    TypeVariable<?> tv = ((Class<?>) GenericType.class).getTypeParameters()[0];
    while (!superclasses.isEmpty()) {
      currentType = superclasses.pop();
      if (currentType instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) currentType;
        Class<?> rawType = (Class<?>) pt.getRawType();
        int argIndex = Arrays.asList(rawType.getTypeParameters()).indexOf(tv);
        if (argIndex > -1) {
          Type typeArg = pt.getActualTypeArguments()[argIndex];
          if (typeArg instanceof TypeVariable) {
            // type argument is another type variable - look for the value of that variable in
            // subclasses
            tv = (TypeVariable<?>) typeArg;
            continue;
          } else {
            // found the value - return it
            return typeArg;
          }
        }
      }
      // needed type argument not supplied - break and throw exception
      break;
    }
    throw new IllegalArgumentException(currentType + " does not specify the type parameter T of GenericType<T>");
  }
}
