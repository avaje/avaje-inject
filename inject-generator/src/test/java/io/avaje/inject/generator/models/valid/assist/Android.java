package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assist;
import io.avaje.inject.Assisted;
import io.avaje.inject.generator.models.valid._Wiring;

@Assisted
public class Android {

  private final int personality;
  private final Model model;
  private final _Wiring wiring;

  public Android(@Assist int personality, @Assist Model model, _Wiring wiring) {
    this.personality = personality;
    this.model = model;
    this.wiring = wiring;
  }
}
