package org.example.myapp.assist.droid;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("tomato")
@AssistFactory(ACarFactory.class)
public class ACar {

  final Paint paint;
  final Engine engine;
  @Assisted List<String> type;
  @Inject Wheel wheel;
  Radio radio;

  public ACar(@Assisted Paint paint, @Nullable Engine engine) {
    this.paint = paint;
    this.engine = engine;
  }

  @Inject
  void injectMethod(@Assisted int size, Radio radio) {
    this.radio = radio;
  }
}
