package io.avaje.inject.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

final class BeanConditions {

  final Set<String> requireTypes = new HashSet<>();
  final Set<String> missingTypes = new HashSet<>();
  final Set<String> qualifierNames = new HashSet<>();
  final Set<String> containsProps = new HashSet<>();
  final Set<String> missingProps = new HashSet<>();
  final Set<String> orProfiles = new HashSet<>();
  final Set<String> andProfiles = new HashSet<>();
  final Set<String> notProfiles = new HashSet<>();
  final Map<String, String> propertyEquals = new HashMap<>();
  final Map<String, String> propertyNotEquals = new HashMap<>();

  void readAll(Element element) {
    readAllDirect(element);
    readMetaAnnotations(element);
  }

  private void readAllDirect(Element element) {
    RequiresBeanPrism.getAllInstancesOn(element).forEach(this::read);
    RequiresBeanContainerPrism.getOptionalOn(element)
        .ifPresent(container -> container.value().forEach(this::read));
    RequiresPropertyPrism.getAllInstancesOn(element).forEach(this::read);
    RequiresPropertyContainerPrism.getOptionalOn(element)
        .ifPresent(container -> container.value().forEach(this::read));
    ProfilePrism.getOptionalOn(element).ifPresent(this::read);
  }

  private void readMetaAnnotations(Element element) {
    RequiresBeanPrism.getAllOnMetaAnnotations(element).forEach(this::read);
    RequiresBeanContainerPrism.getAllOnMetaAnnotations(element).stream()
        .flatMap(e -> e.value().stream())
        .forEach(this::read);
    RequiresPropertyPrism.getAllOnMetaAnnotations(element).forEach(this::read);
    RequiresPropertyContainerPrism.getAllOnMetaAnnotations(element).stream()
        .flatMap(e -> e.value().stream())
        .forEach(this::read);
    ProfilePrism.getAllOnMetaAnnotations(element).forEach(this::read);
  }

  private void read(ProfilePrism prism) {
    orProfiles.addAll(prism.value());
    andProfiles.addAll(prism.all());
    notProfiles.addAll(prism.none());
  }

  private void read(RequiresBeanPrism prism) {
    prism.value().forEach(t -> requireTypes.add(t.toString()));
    prism.missing().forEach(t -> missingTypes.add(t.toString()));
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
    missingProps.addAll(prism.missing());
  }

  void addImports(ImportTypeMap importTypes) {
    requireTypes.forEach(importTypes::add);
    missingTypes.forEach(importTypes::add);

    if (!orProfiles.isEmpty() || !andProfiles.isEmpty() || !notProfiles.isEmpty()) {
      importTypes.add("java.util.List");
    }
  }

  boolean isEmpty() {
    return orProfiles.isEmpty()
        && requireTypes.isEmpty()
        && missingTypes.isEmpty()
        && qualifierNames.isEmpty()
        && containsProps.isEmpty()
        && missingProps.isEmpty()
        && propertyEquals.isEmpty()
        && propertyNotEquals.isEmpty();
  }
}
