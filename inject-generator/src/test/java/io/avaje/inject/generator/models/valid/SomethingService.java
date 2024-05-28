package io.avaje.inject.generator.models.valid;

import java.util.List;

import io.avaje.inject.generator.models.valid.ListFactory.Something;
import jakarta.inject.Singleton;

@Singleton
public class SomethingService {

  List<Something> list;

  public SomethingService(List<Something> list) {
    this.list = list;
  }
}
