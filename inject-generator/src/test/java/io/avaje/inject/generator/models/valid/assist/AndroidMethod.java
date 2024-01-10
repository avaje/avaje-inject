package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import io.avaje.inject.generator.models.valid._Wiring;
import jakarta.inject.Inject;

@AssistFactory
public class AndroidMethod {

  private int personality;
  private Model model;
  private _Wiring wiring;

  @Inject
  public void inject(@Assisted int personalityMatrix, @Assisted Model model, _Wiring wiring) {
    this.personality = personalityMatrix;
    this.model = model;
    this.wiring = wiring;
  }
}
