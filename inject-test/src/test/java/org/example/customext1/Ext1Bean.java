package org.example.customext1;

import org.example.customext0.Ext0Other;
import org.example.customext0.Ext0conc;
import org.example.customext0.Ext0iface;

@Ext1Scope
public class Ext1Bean {

  final Ext0iface ext0iface;
  final Ext0conc ext0conc;
  final Ext0Other ext0Other;
  final Ext1iface ext1iface;
  final Ext1conc ext1conc;

  public Ext1Bean(Ext0iface ext0iface, Ext0conc ext0conc, Ext0Other ext0Other, Ext1iface ext1iface, Ext1conc ext1conc) {
    this.ext0iface = ext0iface;
    this.ext0conc = ext0conc;
    this.ext0Other = ext0Other;
    this.ext1iface = ext1iface;
    this.ext1conc = ext1conc;
  }

  public Ext0iface ext0iface() {
    return ext0iface;
  }

  public Ext0conc ext0conc() {
    return ext0conc;
  }

  public Ext0Other ext0Other() {
    return ext0Other;
  }

  public Ext1iface ext1iface() {
    return ext1iface;
  }

  public Ext1conc ext1conc() {
    return ext1conc;
  }
}
