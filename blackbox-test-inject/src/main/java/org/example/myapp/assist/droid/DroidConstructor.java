package org.example.myapp.assist.droid;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import jakarta.inject.Named;

@Named("android")
@AssistFactory(DroidFactory.class)
public class DroidConstructor implements DroidFactory.Droid {

  private final int personality;
  private final Model model;
  private final Engine wiring;

  public DroidConstructor(@Assisted int personality, @Assisted Model model, Engine wiring) {
    this.personality = personality;
    this.model = model;
    this.wiring = wiring;
  }
}
