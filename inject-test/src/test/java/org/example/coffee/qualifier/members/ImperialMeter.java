package org.example.coffee.qualifier.members;

import org.example.coffee.qualifier.members.TempQualifier.Scale;

import jakarta.inject.Singleton;

@Singleton
@TempQualifier(value = Scale.FAHRENHEIT, someOtherString = "")
public class ImperialMeter implements Thermometer {

  @Override
  public boolean freezing(float temp) {
    return temp <= 32;
  }
}
