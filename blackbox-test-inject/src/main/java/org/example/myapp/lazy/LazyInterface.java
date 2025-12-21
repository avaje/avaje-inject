package org.example.myapp.lazy;

public interface LazyInterface {

  void something();

  void otherThing();

  default boolean isDestroyed() {
    return false;
  }
}
