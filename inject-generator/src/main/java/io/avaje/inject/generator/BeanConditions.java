package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.*;

final class BeanConditions {

  final Set<String> requireTypes = new HashSet<>();
  final Set<String> missingTypes = new HashSet<>();
  final Set<String> qualifierNames = new HashSet<>();
  final Set<String> containsProps = new HashSet<>();
  final Set<String> missingProps = new HashSet<>();
  final Map<String, String> propertyEquals = new HashMap<>();
  final Map<String, String> propertyNotEquals = new HashMap<>();


  void readAll(Element element) {
    readMetaAnnotations(element);
    readAllDirect(element);
  }

  private void readAllDirect(Element element) {
    RequiresBeanPrism.getAllInstancesOn(element).forEach(this::read);
    RequiresBeanContainerPrism.getOptionalOn(element).ifPresent( container -> {
      container.value().forEach(this::read);
    });
    RequiresPropertyPrism.getAllInstancesOn(element).forEach(this::read);
    RequiresPropertyContainerPrism.getOptionalOn(element).ifPresent( container -> {
      container.value().forEach(this::read);
    });
  }

  private void readMetaAnnotations(Element element) {
    element.getAnnotationMirrors().forEach(this::findRequiresOnAnnotation);
  }

  private void findRequiresOnAnnotation(AnnotationMirror a) {
    readAllDirect(a.getAnnotationType().asElement());
  }

  private void read(RequiresBeanPrism prism) {
    prism.value().forEach(t -> requireTypes.add(t.toString()));
    prism.missingBeans().forEach(t -> missingTypes.add(t.toString()));
    qualifierNames.addAll(prism.qualifiers());
  }

  private void read(RequiresPropertyPrism prism) {
    if (!prism.value().isBlank()) {
      if (!prism.notEqualTo().isBlank()) {
        propertyNotEquals.put(prism.value(), prism.notEqualTo());
      } else if (!prism.equalTo().isBlank()) {
        propertyEquals.put(prism.value(), prism.equalTo());
      } else {
        containsProps.add(prism.value());
      }
    }
    missingProps.addAll(prism.missingProperties());
  }

  void addImports(ImportTypeMap importTypes) {
    requireTypes.forEach(importTypes::add);
    missingTypes.forEach(importTypes::add);
  }

  boolean isEmpty() {
    return requireTypes.isEmpty()
      && missingTypes.isEmpty()
      && qualifierNames.isEmpty()
      && containsProps.isEmpty()
      && missingProps.isEmpty()
      && propertyEquals.isEmpty()
      && propertyNotEquals.isEmpty();
  }
}
