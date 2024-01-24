package org.example.coffee.qualifier.members;

import org.example.coffee.qualifier.members.TempQualifier.Scale;

import jakarta.inject.Singleton;

@Singleton
public class Meters {

  Thermometer imperial;

  Thermometer metric;

  public Meters(
      @TempQualifier(value = Scale.FAHRENHEIT, someOtherString = "") Thermometer imperial,
      @TempQualifier(value = Scale.CELSIUS, someOtherString = "") Thermometer metric) {
    this.imperial = imperial;
    this.metric = metric;
  }
}
