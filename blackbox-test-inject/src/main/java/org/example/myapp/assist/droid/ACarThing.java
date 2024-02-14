package org.example.myapp.assist.droid;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class ACarThing {

  final ACarFactory factory;

  ACarThing(ACarFactory factory) {
    this.factory = factory;
  }

  ACar doIt(Paint paint, int size, List<String> other) {
    return factory.construct(paint, size, other);
  }
}
