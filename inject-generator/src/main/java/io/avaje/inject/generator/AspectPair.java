package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import java.util.Set;

class AspectPair implements Comparable<AspectPair> {

  private final String target;
  private final int ordering;
  private final String annotationFullName;
  private final String annotationShortName;

  AspectPair(Element anElement, String target, int ordering) {
    this.target = target;
    this.ordering = ordering;
    this.annotationFullName = anElement.asType().toString();
    this.annotationShortName = Util.shortName(annotationFullName);
  }

  String target() {
    return target;
  }

  void addImports(Set<String> importTypes) {
    importTypes.add(target);
    importTypes.add(annotationFullName);
  }

  String annotationShortName() {
    return annotationShortName;
  }

  @Override
  public int compareTo(AspectPair o) {
    return Integer.compare(ordering, o.ordering);
  }
}
