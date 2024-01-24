package org.example.myapp.assist.droid;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("androidmethod")
@AssistFactory(DroidFactory.class)
public class DroidMethod implements DroidFactory.Droid {

  private int personality;
  private Model model;
  private Engine wiring;

  @Inject
  public void inject(@Assisted int personality, @Assisted Model model, Engine wiring) {
    this.personality = personality;
    this.model = model;
    this.wiring = wiring;
  }
}
