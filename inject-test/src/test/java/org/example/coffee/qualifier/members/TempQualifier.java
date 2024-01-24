package org.example.coffee.qualifier.members;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface TempQualifier {
  Scale value();

  enum Scale {
    CELSIUS,
    FAHRENHEIT,
  }
}
