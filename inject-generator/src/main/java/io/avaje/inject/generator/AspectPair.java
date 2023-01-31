package io.avaje.inject.generator;

import javax.lang.model.element.Element;

final class AspectPair implements Comparable<AspectPair> {

  private final int ordering;
  private final String annotationFullName;
  private final String annotationShortName;

  AspectPair(Element anElement, int ordering) {
    this.ordering = ordering;
    this.annotationFullName = anElement.asType().toString();
    this.annotationShortName = Util.shortName(annotationFullName);
  }

  void addImports(ImportTypeMap importTypes) {
    importTypes.add(Constants.ASPECT_PROVIDER);
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
