/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.avaje.inject.spi;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/** Factory methods for types. */
public class Types {

  private Types() {}

  /** Returns an array type whose elements are all instances of {@code componentType}. */
  public static GenericArrayType arrayOf(Type elementType) {
    return GenericTypeUtil.arrayOf(elementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType parameterizedType(Type rawType, Type... typeArguments) {
    return GenericTypeUtil.newParameterizedType(rawType, typeArguments);
  }

  /** Return the raw type for the given potentially generic type. */
  public static Class<?> rawType(Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      Type rawType = parameterizedType.getRawType();
      return (Class<?>) rawType;

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(rawType(componentType), 0).getClass();

    } else if (type instanceof TypeVariable) {
      return Object.class;

    } else if (type instanceof WildcardType) {
      return rawType(((WildcardType) type).getUpperBounds()[0]);

    } else {

      String className = type == null ? "null" : type.getClass().getName();
      throw new IllegalArgumentException(
          "Expected a Class, ParameterizedType, or  GenericArrayType, but <"
              + type
              + "> is of type "
              + className);
    }
  }
}
