package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assist;
import io.avaje.inject.Assisted;
import io.avaje.lang.Nullable;

@Assisted
public class Car {
  public Car(@Assist Paint paint, @Nullable Engine engine) {}
}
