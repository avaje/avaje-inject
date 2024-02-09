package io.avaje.inject.generator.models.valid.assist;

import java.util.List;

public abstract class CarFactory {

  public abstract Car construct(Paint paint, int size, List<String> type);

  public void nonFactory() {}
}
