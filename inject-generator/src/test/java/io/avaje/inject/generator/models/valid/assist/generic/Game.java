package io.avaje.inject.generator.models.valid.assist.generic;

import io.avaje.inject.generator.models.valid.assist.generic.NightLord.Caligo;
import io.avaje.inject.generator.models.valid.assist.generic.NightLord.Heolstor;
import jakarta.inject.Singleton;

@Singleton
public class Game {
  private LordFactory<Caligo> miasma;
  private LordFactory<Heolstor> lord;

  public Game(LordFactory<Caligo> miasma, LordFactory<Heolstor> lord) {
    super();
    this.miasma = miasma;
    this.lord = lord;
  }
}
