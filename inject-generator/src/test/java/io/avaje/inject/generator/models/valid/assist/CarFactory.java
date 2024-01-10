package io.avaje.inject.generator.models.valid.assist;

import java.util.List;

public interface CarFactory {

  Car create(Paint paint, List<String> type);
}
