package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reads the {@code @DependencyMeta} annotation mirror.
 */
final class ReadDependencyMeta {

  private String type;
  private String name;
  private String method;
  private String providesAspect;
  private String autoProvides;
  private List<String> provides;
  private List<String> dependsOn;

  ReadDependencyMeta(AnnotationMirror mirror) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
      String attrName = entry.getKey().getSimpleName().toString();
      switch (attrName) {
        case "type":
          type = entry.getValue().toString();
          break;
        case "name":
          name = entry.getValue().toString();
          break;
        case "method":
          method = entry.getValue().toString();
          break;
        case "providesAspect":
          providesAspect = entry.getValue().toString();
          break;
        case "autoProvides":
          autoProvides = entry.getValue().toString();
          break;
        case "provides":
          provides = asList(entry.getValue());
          break;
        case "dependsOn":
          dependsOn = asList(entry.getValue());
          break;
        default:
          throw new IllegalStateException("Attribute " + attrName + " is not expected?");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static List<String> asList(AnnotationValue value) {
    final Object entitiesValue = value.getValue();
    if (entitiesValue == null) {
      return Collections.emptyList();
    }
    List<AnnotationValue> values = (List<AnnotationValue>) entitiesValue;
    List<String> result = new ArrayList<>(values.size());
    for (AnnotationValue annotationValue : values) {
      result.add(annotationValue.getValue().toString());
    }
    return result;
  }

  String type() {
    return type;
  }

  String name() {
    return name;
  }

  String method() {
    return method;
  }

  String providesAspect() {
    return providesAspect;
  }

  List<String> provides() {
    return provides;
  }

  String autoProvides() {
    return autoProvides;
  }

  List<String> dependsOn() {
    return dependsOn;
  }
}
