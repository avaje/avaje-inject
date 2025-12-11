package io.avaje.inject.generator;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

final class FieldReader {

  private final Element element;
  private final String name;
  private final UtilType utype;
  private final boolean nullable;
  private final String fieldType;
  private final UType type;
  private boolean requestParam;
  private String requestParamName;
  private final boolean isBeanMap;
  private final boolean assisted;

  FieldReader(Element element) {
    this.element = element;
    this.name = Util.named(element);
    this.nullable = Util.isNullable(element);
    this.isBeanMap = QualifiedMapPrism.isPresent(element);
    this.utype = Util.determineType(element.asType(), isBeanMap);
    this.fieldType = Util.unwrapProvider(utype.rawType(isBeanMap));
    this.type = utype.toUType();
    this.assisted = AssistedPrism.isPresent(element);
    if (nullable || element.asType().toString().startsWith("java.util.Optional<")) {
      ProcessingContext.addOptionalType(fieldType, name);
    }
    if (type.fullWithoutAnnotations().startsWith("io.avaje.inject.events.Event")) {
      EventPublisherWriter.write(element);
    }
  }

  boolean isGenericParam() {
    return type.isGeneric() && !Util.isProvider(type.mainType());
  }

  void addDependsOnGeneric(Set<UType> set) {
    if (isGenericParam()) {
      set.add(type);
    }
  }

  String fieldName() {
    return element.getSimpleName().toString();
  }

  void addImports(ImportTypeMap importTypes) {
    importTypes.addAll(type.importTypes());
  }

  String builderGetDependency(String builder) {
    final var sb = new StringBuilder();
    final boolean wildcard = isWildcard();
    final var wildParam = wildcard ? String.format("<%s>", type.shortWithoutAnnotations()) : "";
    sb.append(builder).append(".").append(wildParam).append(utype.getMethod(nullable, isBeanMap));
    if (!wildcard && isGenericParam()) {
      sb.append("TYPE_").append(Util.shortName(type).replace(".", "_"));
    } else {
      sb.append(Util.shortName(type.mainType())).append(".class");
    }
    if (name != null) {
      sb.append(",\"").append(name).append("\"");
    }
    sb.append(")");
    return sb.toString();
  }


  private boolean isWildcard() {
    return type.isGeneric()
      && type.componentTypes().stream().allMatch(g -> g.kind() == TypeKind.WILDCARD);
  }

  void removeFromProvides(List<UType> provides) {
    provides.remove(type);
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
      writer.append(type.shortType());
      writer.append(" %s;", requestParamName).eol().eol();
    }
  }

  /**
   * Generate code to set bean field dependencies as part of BeanFactory create().
   */
  void writeRequestInject(Append writer) {
    writer.append("    bean.%s = %s;", fieldName(), requestParamName).eol();
  }

  Element element() {
    return element;
  }

  boolean assisted() {
    return assisted;
  }
}
