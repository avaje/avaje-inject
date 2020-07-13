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

    TypeMirror type = element.asType();
    String rawType = type.toString();

    boolean listType = Util.isList(rawType);
    boolean optionalType = !listType && Util.isOptional(rawType);
    if (optionalType) {
      rawType = Util.extractOptionalType(rawType);
    } else if (listType) {
      rawType = Util.extractList(rawType);
    }

    StringBuilder sb = new StringBuilder();
    if (listType) {
      sb.append("b.getList(");
    } else if (optionalType) {
      sb.append("b.getOptional(");
    } else {
      sb.append("b.get(");
    }

    sb.append(rawType).append(".class");
    if (name != null) {
      sb.append(",\"").append(name).append("\"");
    }
    sb.append(")");
    return sb.toString();
  }
}
