package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assist;
import io.avaje.inject.Assisted;
import io.avaje.inject.generator.models.valid._Wiring;

@Assisted
public class Android {

  private final int personalityMatrix;
  private final Model model;
  private final _Wiring wiring;

  public Android(@Assist int personalityMatrix, @Assist Model model, _Wiring wiring) {
    this.personalityMatrix = personalityMatrix;
    this.model = model;
    this.wiring = wiring;
  }
}
