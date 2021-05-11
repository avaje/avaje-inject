package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the inheritance types for a given bean type.
 */
class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final List<String> extendsTypes = new ArrayList<>();
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;

  TypeExtendsReader(TypeElement baseType, ProcessingContext context) {
    this.baseType = baseType;
    this.context = context;
  }

  List<String> getExtendsTypes() {
    return extendsTypes;
  }

  String getQualifierName() {
    return qualifierName;
  }

  void process() {
    String base = Util.unwrapProvider(baseType.getQualifiedName().toString());
    if (!GenericType.isGeneric(base)) {
      extendsTypes.add(base);
    }
    TypeElement superElement = superOf(baseType);
    if (superElement != null) {
      String baseName = baseType.getSimpleName().toString();
      String superName = superElement.getSimpleName().toString();
      if (baseName.endsWith(superName)) {
        qualifierName = baseName.substring(0, baseName.length() - superName.length()).toLowerCase();
      }
      addSuperType(superElement);
    }
  }

  private void addSuperType(TypeElement element) {
    String fullName = element.getQualifiedName().toString();
    if (!fullName.equals(JAVA_LANG_OBJECT)) {
      String type = Util.unwrapProvider(fullName);
      if (!GenericType.isGeneric(type)) {
        extendsTypes.add(type);
        addSuperType(superOf(element));
      }
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) context.asElement(element.getSuperclass());
  }
}
