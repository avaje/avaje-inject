package io.dinject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

class FieldReader {

  private final Element element;

  private final String name;

  FieldReader(Element element) {
    this.element = element;
    this.name = Util.getNamed(element);
  }

  String getFieldName() {
    return element.getSimpleName().toString();
  }

  String builderGetDependency() {

    UtilType beanType = Util.determineType(element.asType());

    StringBuilder sb = new StringBuilder();
    sb.append("b.").append(beanType.getMethod());
    sb.append(beanType.rawType()).append(".class");
    if (name != null) {
      sb.append(",\"").append(name).append("\"");
    }
    sb.append(")");
    return sb.toString();
  }
}
