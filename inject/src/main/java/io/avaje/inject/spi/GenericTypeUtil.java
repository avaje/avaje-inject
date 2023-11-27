package io.avaje.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Set;
import java.util.Stack;

/** Cut down version of GenericTypeUtil from Helidon project Apache 2 license. */
final class GenericTypeUtil {

  private static final Type[] EMPTY_TYPE_ARRAY = {};

  /** Return the Type for type parameter of {@code GenericType<T>}. */
  static Type typeArgument(Class<?> clazz) {
    // collect superclasses
    Stack<Type> superclasses = new Stack<>();
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
    throw new IllegalArgumentException(
        currentType + " does not specify the type parameter T of GenericType<T>");
  }

  /** Returns an array type whose elements are all instances of {@code componentType}. */
  public static GenericArrayType arrayOf(Type elementType) {
    return new GenericArrayTypeImpl(elementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
    if (typeArguments.length == 0) {
      throw new IllegalArgumentException("Missing type arguments for " + rawType);
    }
    return new ParameterizedTypeImpl(null, rawType, typeArguments);
  }

  static Type canonicalizeClass(Class<?> cls) {
    return cls.isArray() ? new GenericArrayTypeImpl(canonicalize(cls.getComponentType())) : cls;
  }

  /**
   * Returns a type that is functionally equal but not necessarily equal according to {@link
   * Object#equals(Object) Object.equals()}.
   */
  static Type canonicalize(Type type) {
    if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

    } else if (type instanceof ParameterizedType) {
      if (type instanceof ParameterizedTypeImpl) return type;
      ParameterizedType p = (ParameterizedType) type;
      return new ParameterizedTypeImpl(
          p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());

    } else if (type instanceof GenericArrayType) {
      if (type instanceof GenericArrayTypeImpl) return type;
      GenericArrayType g = (GenericArrayType) type;
      return new GenericArrayTypeImpl(g.getGenericComponentType());

    } else if (type instanceof WildcardType) {
      if (type instanceof WildcardTypeImpl) return type;
      WildcardType w = (WildcardType) type;
      return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

    } else {
      return type; // This type is unsupported!
    }
  }

  static int hashCodeOrZero(Object o) {
    return o != null ? o.hashCode() : 0;
  }

  static String typeToString(Type type) {
    return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
  }

  static void checkNotPrimitive(Type type) {
    if ((type instanceof Class<?>) && ((Class<?>) type).isPrimitive()) {
      throw new IllegalArgumentException("Unexpected primitive " + type + ". Use the boxed type.");
    }
  }

  static final class ParameterizedTypeImpl implements ParameterizedType {
    private final Type ownerType;
    private final Type rawType;
    public final Type[] typeArguments;

    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
      // Require an owner type if the raw type needs it.
      if (ownerType != null && rawType instanceof Class<?>) {
        Class<?> enclosingClass = ((Class<?>) rawType).getEnclosingClass();
        if (enclosingClass == null || Types.rawType(ownerType) != enclosingClass) {
          throw new IllegalArgumentException(
              "unexpected owner type for " + rawType + ": " + ownerType);

        } else if (enclosingClass != null) {
          throw new IllegalArgumentException("unexpected owner type for " + rawType + ": null");
        }
      }

      this.ownerType = ownerType == null ? null : canonicalize(ownerType);
      this.rawType = canonicalize(rawType);
      this.typeArguments = typeArguments.clone();
      for (int t = 0; t < this.typeArguments.length; t++) {
        if (this.typeArguments[t] == null) throw new NullPointerException();
        checkNotPrimitive(this.typeArguments[t]);
        this.typeArguments[t] = canonicalize(this.typeArguments[t]);
      }
    }

    @Override
    public Type[] getActualTypeArguments() {
      return typeArguments.clone();
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof ParameterizedType
          && GenericTypeUtil.equals(this, (ParameterizedType) other);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
      result.append(typeToString(rawType));

      if (typeArguments.length == 0) {
        return result.toString();
      }

      result.append("<").append(typeToString(typeArguments[0]));
      for (int i = 1; i < typeArguments.length; i++) {
        result.append(", ").append(typeToString(typeArguments[i]));
      }
      return result.append(">").toString();
    }
  }

  static final class GenericArrayTypeImpl implements GenericArrayType {
    private final Type componentType;

    GenericArrayTypeImpl(Type componentType) {
      this.componentType = canonicalize(componentType);
    }

    @Override
    public Type getGenericComponentType() {
      return componentType;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof GenericArrayType && GenericTypeUtil.equals(this, (GenericArrayType) o);
    }

    @Override
    public int hashCode() {
      return componentType.hashCode();
    }

    @Override
    public String toString() {
      return typeToString(componentType) + "[]";
    }
  }

  /**
   * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only
   * support what the Java 6 language needs - at most one bound. If a lower bound is set, the upper
   * bound must be Object.class.
   */
  static final class WildcardTypeImpl implements WildcardType {
    private final Type upperBound;
    private final Type lowerBound;

    WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      if ((lowerBounds.length > 1) || (upperBounds.length != 1))
        throw new IllegalArgumentException();

      if (lowerBounds.length == 1) {
        if (lowerBounds[0] == null) throw new NullPointerException();
        checkNotPrimitive(lowerBounds[0]);
        if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
        this.lowerBound = canonicalize(lowerBounds[0]);
        this.upperBound = Object.class;

      } else {
        if (upperBounds[0] == null) throw new NullPointerException();
        checkNotPrimitive(upperBounds[0]);
        this.lowerBound = null;
        this.upperBound = canonicalize(upperBounds[0]);
      }
    }

    @Override
    public Type[] getUpperBounds() {
      return new Type[] {upperBound};
    }

    @Override
    public Type[] getLowerBounds() {
      return lowerBound != null ? new Type[] {lowerBound} : EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof WildcardType && GenericTypeUtil.equals(this, (WildcardType) other);
    }

    @Override
    public int hashCode() {
      // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
      return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
    }

    @Override
    public String toString() {
      if (lowerBound != null) {
        return "? super " + typeToString(lowerBound);
      } else if (upperBound == Object.class) {
        return "?";
      } else {
        return "? extends " + typeToString(upperBound);
      }
    }
  }

  static String typeAnnotatedWithAnnotations(Type type, Set<? extends Annotation> annotations) {
    return type + (annotations.isEmpty() ? " (with no annotations)" : " annotated " + annotations);
  }

  /**
   * Returns a type that represents an unknown type that extends {@code bound}. For example, if
   * {@code bound} is {@code CharSequence.class}, this returns {@code ? extends CharSequence}. If
   * {@code bound} is {@code Object.class}, this returns {@code ?}, which is shorthand for {@code ?
   * extends Object}.
   */
  static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
    if (bound instanceof WildcardType) {
      upperBounds = ((WildcardType) bound).getUpperBounds();
    } else {
      upperBounds = new Type[] {bound};
    }
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }

  /**
   * Returns a type that represents an unknown supertype of {@code bound}. For example, if {@code
   * bound} is {@code String.class}, this returns {@code ? super String}.
   */
  static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
    if (bound instanceof WildcardType) {
      lowerBounds = ((WildcardType) bound).getLowerBounds();
    } else {
      lowerBounds = new Type[] {bound};
    }
    return new WildcardTypeImpl(new Type[] {Object.class}, lowerBounds);
  }

  /** Returns true if {@code a} and {@code b} are equal. */
  static boolean equals(Type a, Type b) {
    if (a == b) {
      return true; // Also handles (a == null && b == null).

    } else if (a instanceof Class) {
      if (b instanceof GenericArrayType) {
        return equals(
            ((Class<?>) a).getComponentType(), ((GenericArrayType) b).getGenericComponentType());
      }
      return a.equals(b); // Class already specifies equals().

    } else if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType)) return false;
      ParameterizedType pa = (ParameterizedType) a;
      ParameterizedType pb = (ParameterizedType) b;
      Type[] aTypeArguments =
          pa instanceof ParameterizedTypeImpl
              ? ((ParameterizedTypeImpl) pa).typeArguments
              : pa.getActualTypeArguments();
      Type[] bTypeArguments =
          pb instanceof ParameterizedTypeImpl
              ? ((ParameterizedTypeImpl) pb).typeArguments
              : pb.getActualTypeArguments();
      return equals(pa.getOwnerType(), pb.getOwnerType())
          && pa.getRawType().equals(pb.getRawType())
          && Arrays.equals(aTypeArguments, bTypeArguments);

    } else if (a instanceof GenericArrayType) {
      if (b instanceof Class) {
        return equals(
            ((Class<?>) b).getComponentType(), ((GenericArrayType) a).getGenericComponentType());
      }
      if (!(b instanceof GenericArrayType)) return false;
      GenericArrayType ga = (GenericArrayType) a;
      GenericArrayType gb = (GenericArrayType) b;
      return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

    } else if (a instanceof WildcardType) {
      if (!(b instanceof WildcardType)) return false;
      WildcardType wa = (WildcardType) a;
      WildcardType wb = (WildcardType) b;
      return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
          && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

    } else if (a instanceof TypeVariable) {
      if (!(b instanceof TypeVariable)) return false;
      TypeVariable<?> va = (TypeVariable<?>) a;
      TypeVariable<?> vb = (TypeVariable<?>) b;
      return va.getGenericDeclaration() == vb.getGenericDeclaration()
          && va.getName().equals(vb.getName());

    } else {
      // This isn't a supported type.
      return false;
    }
  }
}
