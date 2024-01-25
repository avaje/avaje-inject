package org.example.coffee.qualifier.members;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface TempQualifier {
  Scale[] value();

  String someOtherString();

  int defaultVal() default 0;

  NestedAnnotation[] inject() default {@NestedAnnotation()};

  enum Scale {
    CELSIUS,
    FAHRENHEIT,
  }

  @interface NestedAnnotation {

    Inject[] inject() default {};
  }
}
