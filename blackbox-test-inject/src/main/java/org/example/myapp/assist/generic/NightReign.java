package org.example.myapp.assist.generic;

import org.example.myapp.assist.generic.NightLord.Caligo;
import org.example.myapp.assist.generic.NightLord.Libra;

import jakarta.inject.Singleton;

@Singleton
public class NightReign {

  private LordFactory<Caligo> caligo;
  private LordFactory<Libra> creature;

  public NightReign(LordFactory<Caligo> caligo, LordFactory<Libra> creature) {
    this.caligo = caligo;
    this.creature = creature;
  }

  public Caligo caligo() {
    return caligo.create("Miasma of Night");
  }

  public Libra libra() {
    return creature.create("Creature of Night");
  }
}
