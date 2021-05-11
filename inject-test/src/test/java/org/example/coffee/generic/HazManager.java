package org.example.coffee.generic;

import javax.inject.Inject;
import javax.inject.Singleton;

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
