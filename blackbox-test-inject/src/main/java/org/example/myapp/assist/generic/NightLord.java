package org.example.myapp.assist.generic;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;

public interface NightLord {

  @AssistFactory(LordFactory.class)
  public static class Caligo implements NightLord {

    @Assisted String title;
  }

  @AssistFactory(LordFactory.class)
  public static class Libra implements NightLord {

    @Assisted String title;
  }
}
