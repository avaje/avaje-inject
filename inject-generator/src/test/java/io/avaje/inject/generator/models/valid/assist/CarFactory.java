package io.avaje.inject.generator.models.valid.assist;

import java.util.List;

public interface CarFactory {
  Car construct(Paint paint, int size, List<String> type);
}
