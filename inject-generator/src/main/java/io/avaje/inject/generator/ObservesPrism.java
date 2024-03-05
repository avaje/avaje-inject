package io.avaje.inject.generator;

import java.util.Optional;

import javax.lang.model.element.Element;

import io.avaje.inject.event.Observes;
import io.avaje.inject.event.ObservesAsync;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = Observes.class,
    name = "ObservesSyncPrism",
    superInterfaces = ObservesPrism.class)
@GeneratePrism(value = ObservesAsync.class, superInterfaces = ObservesPrism.class)
public interface ObservesPrism {

  static boolean isPresent(Element e) {
    return ObservesSyncPrism.isPresent(e) || ObservesAsyncPrism.isPresent(e);
  }

  Integer priority();

  static Optional<ObservesPrism> getOptionalOn(Element e) {

    return Optional.<ObservesPrism>empty()
        .or(() -> ObservesSyncPrism.getOptionalOn(e))
        .or(() -> ObservesAsyncPrism.getOptionalOn(e));
  }

  static ObservesPrism getInstanceOn(Element e) {

    return Optional.<ObservesPrism>ofNullable(ObservesSyncPrism.getInstanceOn(e))
        .orElse(ObservesAsyncPrism.getInstanceOn(e));
  }

  default boolean async() {
    return this instanceof ObservesAsyncPrism;
  }
}
