package org.example.myapp.assist.droid;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("androidfield")
@AssistFactory(DroidFactory.class)
public class DroidField implements DroidFactory.Droid {
  @Assisted int personality;
  @Assisted Model model;
  @Inject Engine wiring;

  @Inject Radio radio;

  @Override
  public boolean dependenciesAreWired() {
    return wiring != null && radio != null;
  }
}
