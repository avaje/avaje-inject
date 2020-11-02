package org.example.coffee.priority.custom;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE,PARAMETER})
@Retention(RUNTIME)
public @interface CustomPriority {
  int value();
}
