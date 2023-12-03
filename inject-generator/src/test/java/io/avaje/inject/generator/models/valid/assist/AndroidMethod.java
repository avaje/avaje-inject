package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assist;
import io.avaje.inject.Assisted;
import io.avaje.inject.generator.models.valid._Wiring;
import jakarta.inject.Inject;

@Assisted
public class AndroidMethod {

  private int personality;
  private Model model;
  private _Wiring wiring;

  @Inject
  public void inject(@Assist int personalityMatrix, @Assist Model model, _Wiring wiring) {
    this.personality = personalityMatrix;
    this.model = model;
    this.wiring = wiring;
  }
}
