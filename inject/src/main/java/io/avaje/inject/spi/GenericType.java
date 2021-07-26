package io.avaje.inject.spi;

import java.lang.reflect.Type;

/**
 * Represents a full type including generics declaration, to avoid information loss due to type erasure.
 * <p>
 * This is a cut down version of Helidon GenericType Apache 2 licence.
 *
 * @param <T> the generic type parameter
 */
public class GenericType<T> implements Type {

  private final Type type;

  /**
   * Constructs a new generic type, deriving the generic type and class from type parameter.
   */
  protected GenericType() throws IllegalArgumentException {
    this.type = GenericTypeUtil.typeArgument(getClass());
  }

  /**
   * Return the type represented by this generic type instance.
   */
  public Type type() {
    return type;
  }

  @Override
  public String getTypeName() {
    return type.toString();
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof GenericType) {
      return ((GenericType<?>) obj).type.equals(this.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
