package org.example.coffee.generic;

import org.example.coffee.grind.AMusher;

import javax.inject.Singleton;

/**
 * Has multiple dependencies that are generic interface.
 */
@Singleton
class MultiGenericConsumer {

  private final Repository<Haz, Long> hazRepo;

  private final SomeGeneric<String> stringProcessor;

  private final AMusher aMusher;

  MultiGenericConsumer(Repository<Haz, Long> hazRepo, AMusher aMusher, SomeGeneric<String> stringProcessor) {
    this.hazRepo = hazRepo;
    this.aMusher = aMusher;
    this.stringProcessor = stringProcessor;
  }

  String findAndDo(long id) {
    final Haz byId = hazRepo.findById(id);
    return (byId == null) ? "not found" : "found " + stringProcessor.process("" + byId.id);
  }

  String mushString() {
    return aMusher.toString();
  }
}
