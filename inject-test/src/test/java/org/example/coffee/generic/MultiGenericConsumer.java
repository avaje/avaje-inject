package org.example.coffee.generic;

import java.util.List;

import org.example.coffee.grind.AMusher;

import jakarta.inject.Singleton;

/**
 * Has multiple dependencies that are generic interface.
 */
@Singleton
class MultiGenericConsumer {

  private final Repository<Haz, Long> hazRepo;

  private final SomeGeneric<String> stringProcessor;

  private final AMusher aMusher;

  private List<SomeGeneric<?>> list;

  MultiGenericConsumer(
      Repository<Haz, Long> hazRepo,
      AMusher aMusher,
      SomeGeneric<String> stringProcessor,
      List<SomeGeneric<?>> list) {
    this.hazRepo = hazRepo;
    this.aMusher = aMusher;
    this.stringProcessor = stringProcessor;
    this.list = list;
  }

  String findAndDo(long id) {
    final Haz byId = hazRepo.findById(id);
    return byId == null ? "not found" : "found " + stringProcessor.process("" + byId.id);
  }

  String mushString() {
    return aMusher.toString();
  }

  public List<SomeGeneric<?>> list() {
    return list;
  }
}
