package io.avaje.inject.events;

public @interface Observes {

  boolean async() default false;
}
