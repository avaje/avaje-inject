package io.avaje.inject.generator.models.valid.assist.generic;

public interface LordFactory<N extends NightLord> {
  N create(String title);
}
