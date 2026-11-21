package io.avaje.inject.generator;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.lang.model.element.Element;

final class InstanceInjectorMetaData {

  private final String target;
  private final String injector;
  private final List<Dependency> dependsOn;
  private final Element sourceElement;

  InstanceInjectorMetaData(InstanceInjectorReader reader) {
    this.target = reader.targetName();
    this.injector = reader.generatedTypeName();
    this.dependsOn = reader.dependsOn();
    this.sourceElement = reader.targetType();
  }

  InstanceInjectorMetaData(InstanceInjectorMetaPrism prism) {
    this.target = prism.target();
    this.injector = prism.injector();
    this.dependsOn = prism.dependsOn().stream().map(Dependency::new).collect(toList());
    this.sourceElement = null;
  }

  String target() {
    return target;
  }

  String injector() {
    return injector;
  }

  String packageName() {
    return ProcessorUtils.packageOf(injector);
  }

  List<Dependency> dependsOn() {
    return dependsOn;
  }

  Element sourceElement() {
    return sourceElement;
  }

  void markWithExternalDependency(String name) {
    for (Dependency dependency : dependsOn) {
      if (name.equals(dependency.name())) {
        dependency.markExternal();
      }
    }
  }

  void writeAnnotation(Append writer) {
    writer.append("  @InstanceInjectorMeta(").eol();
    writer.append("      target = \"%s\",", target).eol();
    if (dependsOn.isEmpty()) {
      writer.append("      injector = \"%s\")", injector).eol();
      return;
    }
    writer.append("      injector = \"%s\",", injector).eol();
    writer.append("      dependsOn = {");
    if (dependsOn.size() > 1) {
      writer.eol().append("        ");
    }
    for (int i = 0; i < dependsOn.size(); i++) {
      if (i > 0) {
        writer.append(",").eol().append("        ");
      }
      writer.append("\"%s\"", dependsOn.get(i).dependsOn());
    }
    if (dependsOn.size() > 1) {
      writer.eol().append("      ");
    }
    writer.append("})").eol();
  }
}
