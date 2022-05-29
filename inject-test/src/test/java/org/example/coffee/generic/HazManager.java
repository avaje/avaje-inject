package org.example.coffee.generic;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Has a Dependency via generic interface.
 */
@Singleton
public class HazManager {

  private final Repository<Haz, Long> hazRepo;

  @Inject
  public HazManager(Repository<Haz, Long> hazRepo) {
    this.hazRepo = hazRepo;
  }

  public Haz find(Long id) {
    return hazRepo.findById(id);
  }
}
