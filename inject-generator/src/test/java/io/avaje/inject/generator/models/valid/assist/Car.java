package io.avaje.inject.generator.models.valid.assist;

import java.util.List;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import io.avaje.lang.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("tomato")
@AssistFactory(CarFactory.class)
public class Car {

  @Assisted List<String> type;
  @Inject Wheel wheel;

  public Car(@Assisted Paint paint, @Nullable Engine engine) {}

  @Inject
  void injectMethod(@Assisted int size, Model m) {}
}
