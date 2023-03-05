package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Set;

final class FieldReader {

  private final Element element;
  private final String name;
  private final UtilType utype;
  private final boolean nullable;
  private final String fieldType;
  private final GenericType type;
  private boolean requestParam;
  private String requestParamName;
  private final boolean isBeanMap;

  FieldReader(Element element) {
    this.element = element;
    this.name = Util.getNamed(element);
    this.nullable = Util.isNullable(element);
    this.utype = Util.determineType(element.asType());
    this.isBeanMap = QualifiedMapPrism.isPresent(element);
    this.fieldType = Util.unwrapProvider(utype.rawType(isBeanMap));
    this.type = GenericType.parse(utype.rawType(isBeanMap));
    if (nullable || element.asType().toString().startsWith("java.util.Optional<"))
      ProcessingContext.getOptionalTypes().add(fieldType);
  }

  boolean isGenericParam() {
    return type.isGenericType() && !type.isProviderType();
  }

  void addDependsOnGeneric(Set<GenericType> set) {
    if (isGenericParam()) {
      set.add(type);
    }
  }

  String fieldName() {
    return element.getSimpleName().toString();
  }

  void addImports(ImportTypeMap importTypes) {
    type.addImports(importTypes);
  }

  String builderGetDependency(String builder) {
    StringBuilder sb = new StringBuilder();
    sb.append(builder).append(".").append(utype.getMethod(nullable, isBeanMap));
    if (isGenericParam()) {
      sb.append("TYPE_").append(type.shortName());
    } else {
      sb.append(Util.shortName(fieldType)).append(".class");
    }
    if (name != null) {
      sb.append(",\"").append(name).append("\"");
    }
    sb.append(")");
    return sb.toString();
  }

  void removeFromProvides(List<String> provides) {
    provides.remove(type.toString());
  }

  /**
   * Check for request scoped dependency.
   */
  void checkRequest(BeanRequestParams requestParams) {
    requestParam = requestParams.check(utype.rawType(isBeanMap));
    if (requestParam) {
      requestParamName = requestParams.argumentName(utype.rawType(isBeanMap));
    }
  }

  /**
   * Generate code for dependency inject for BeanFactory.
   */
  void writeRequestDependency(Append writer) {
    if (!requestParam) {
      // just add as field dependency
      requestParamName = writer.nextName(fieldName().toLowerCase());
      writer.append("  @Inject").eol().append("  ");
      type.writeShort(writer);
      writer.append(" %s;", requestParamName).eol().eol();
    }
  }

  /**
   * Generate code to set bean field dependencies as part of BeanFactory create().
   */
  void writeRequestInject(Append writer) {
    writer.append("    bean.%s = %s;", fieldName(), requestParamName).eol();
  }

}
