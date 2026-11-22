package io.avaje.inject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.spi.Injector;

/** The {@code Injector<T>} type used to look up generated injectors for a given target type. */
final class InjectorType implements ParameterizedType {

  private final Type[] args;

  private InjectorType(Class<?> target) {
    this.args = new Type[] {target};
  }

  /**
   * Find the most specific {@code Injector} for the given type, searching the type itself, then its
   * superclasses and then its interfaces.
   */
  static <T> Injector<T> find(BeanScope scope, Class<?> type) {
    for (final var candidate : candidates(type)) {
      final Injector<T> injector = scope.getNullable(new InjectorType(candidate), null);
      if (injector != null) {
        return injector;
      }
    }
    throw new NoSuchElementException(
        "No Injector found for type: "
            + type.getName()
            + " - add @InjectorTarget to the type (or a supertype) or use @InjectorTarget.Import");
  }

  private static Set<Class<?>> candidates(Class<?> type) {
    final Set<Class<?>> result = new LinkedHashSet<>();
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      result.add(c);
    }
    final Deque<Class<?>> queue = new ArrayDeque<>();
    for (Class<?> c = type; c != null; c = c.getSuperclass()) {
      queue.addAll(Arrays.asList(c.getInterfaces()));
    }
    while (!queue.isEmpty()) {
      final Class<?> iface = queue.poll();
      if (result.add(iface)) {
        queue.addAll(Arrays.asList(iface.getInterfaces()));
      }
    }
    return result;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return args.clone();
  }

  @Override
  public Type getRawType() {
    return Injector.class;
  }

  @Override
  public @Nullable Type getOwnerType() {
    return null;
  }

  @Override
  public String getTypeName() {
    return Injector.class.getName() + "<" + args[0].getTypeName() + ">";
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ParameterizedType)) {
      return false;
    }
    final var other = (ParameterizedType) obj;
    return other.getOwnerType() == null
        && Injector.class.equals(other.getRawType())
        && Arrays.equals(args, other.getActualTypeArguments());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(args) ^ Injector.class.hashCode();
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
