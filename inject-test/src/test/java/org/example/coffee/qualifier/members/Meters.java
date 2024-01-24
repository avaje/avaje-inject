package org.example.coffee.qualifier.members;

import org.example.coffee.qualifier.members.TempQualifier.Scale;

import jakarta.inject.Singleton;

@Singleton
public class Meters {

  Thermometer imperial;

  Thermometer metric;

  public Meters(
      @TempQualifier(Scale.FAHRENHEIT) Thermometer imperial,
      @TempQualifier(Scale.CELSIUS) Thermometer metric) {
    this.imperial = imperial;
    this.metric = metric;
  }
}
