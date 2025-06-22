package io.avaje.inject.generator.models.valid.assist.generic;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;

public interface NightLord {

  @AssistFactory(LordFactory.class)
  public static class Caligo implements NightLord {

    @Assisted String title;
  }

  @AssistFactory(LordFactory.class)
  public static class Heolstor implements NightLord {

    @Assisted String title;
  }
}
