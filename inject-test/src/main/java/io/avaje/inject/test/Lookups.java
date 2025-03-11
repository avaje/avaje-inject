package io.avaje.inject.test;

import static java.util.stream.Collectors.toMap;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.ServiceLoader;

/** Provides Lookup instances using potentially module specific Lookups. */
final class Lookups {

  private static final Map<String, Lookup> MODULE_LOOKUP_MAP =
      ServiceLoader.load(LookupProvider.class).stream()
          .collect(toMap(p -> p.type().getModule().getName(), p -> p.get().provideLookup()));

  private static final Lookup DEFAULT_LOOKUP = MethodHandles.publicLookup();

  /** Return a Lookup ideally for the module associated with the given type. */
  static Lookup getLookup(Class<?> type) {
    return MODULE_LOOKUP_MAP.getOrDefault(type.getModule().getName(), DEFAULT_LOOKUP);
  }

  static VarHandle getVarhandle(Class<?> testClass, Field field) {
    try {
      var lookup = getLookup(testClass);
      lookup =
          lookup.hasPrivateAccess()
              ? MethodHandles.privateLookupIn(testClass, getLookup(testClass))
              : lookup;

      return lookup.unreflectVarHandle(field);
    } catch (Exception e) {
      throw new IllegalStateException("Can't access field " + field, e);
    }
  }

  static Class<?> getClassFromType(Type generic) {
    if (generic instanceof Class) {
      return (Class<?>) generic;
    }
    if (generic instanceof ParameterizedType) {
      Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
      if (actual instanceof Class) {
        return (Class<?>) actual;
      }
      if (actual instanceof ParameterizedType) {
        return (Class<?>) ((ParameterizedType) actual).getRawType();
      }
    }
    return Object.class;
  }
}
