package io.avaje.inject.generator.models.valid.assist;

import java.util.List;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import io.avaje.lang.Nullable;
import jakarta.inject.Named;

@Named("tomato")
@AssistFactory(CarFactory.class)
public class Car {
  public Car(@Assisted Paint paint, @Assisted List<String> type, @Nullable Engine engine) {}
}
