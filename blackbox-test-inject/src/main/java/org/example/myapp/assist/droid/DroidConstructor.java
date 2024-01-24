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
  private final Radio radio;

  public DroidConstructor(@Assisted int personality, @Assisted Model model, Engine wiring, @Named("red") Radio radio) {
    this.personality = personality;
    this.model = model;
    this.wiring = wiring;
    this.radio = radio;
  }

  public int personality() {
    return personality;
  }

  public Model model() {
    return model;
  }

  @Override
  public boolean dependenciesAreWired() {
    return wiring != null && radio != null;
  }
}
