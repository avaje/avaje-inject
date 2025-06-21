package org.example.myapp.assist.generic;

import org.example.myapp.assist.generic.NightLord.Caligo;
import org.example.myapp.assist.generic.NightLord.Libra;

import jakarta.inject.Singleton;

@Singleton
public class NightReign {

  private LordFactory<Caligo> caligoFactory;
  private LordFactory<Libra> libraFactory;

  public NightReign(LordFactory<Caligo> caligoFactory, LordFactory<Libra> libraFactory) {
    this.caligoFactory = caligoFactory;
    this.libraFactory = libraFactory;
  }

  public Caligo caligo() {
    return caligoFactory.create("Miasma of Night");
  }

  public Libra libra() {
    return libraFactory.create("Creature of Night");
  }
}
