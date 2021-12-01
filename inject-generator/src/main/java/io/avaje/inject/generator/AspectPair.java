package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import java.util.Set;

class AspectPair {
  private final Element anElement;
  private final String target;

  AspectPair(Element anElement, String target) {
    this.anElement = anElement;
    this.target = target;
  }

  String target() {
    return target;
  }

  void addImports(Set<String> importTypes) {
    importTypes.add(target);
  }
}
