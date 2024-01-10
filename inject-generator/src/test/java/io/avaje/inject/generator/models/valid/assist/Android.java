package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import io.avaje.inject.generator.models.valid._Wiring;

@AssistFactory
public class Android {

  private final int personality;
  private final Model model;
  private final _Wiring wiring;

  public Android(@Assisted int personality, @Assisted Model model, _Wiring wiring) {
    this.personality = personality;
    this.model = model;
    this.wiring = wiring;
  }
}
