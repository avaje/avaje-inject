package io.avaje.inject.generator.models.valid.qualifier;

import io.avaje.inject.generator.models.valid.qualifier.TempQualifier.Scale;
import jakarta.inject.Singleton;

@Singleton
@TempQualifier(value = Scale.FAHRENHEIT, someOtherString = "far")
public class ImperialMeter implements Thermometer {

  @Override
  public boolean freezing(float temp) {
    return temp <= 32;
  }
}
