package io.avaje.inject.generator;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

final class InjectorBeanReader {

  private final TypeElement beanType;
  private final String type;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final TypeReader typeReader;
  private final Element preDestroyMethod;

  InjectorBeanReader(TypeElement beanType) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.typeReader =
        new TypeReader(List.of(), UType.parse(beanType.asType()), beanType, importTypes, false);
    typeReader.process();
    this.injectFields = typeReader.injectFields();
    this.injectMethods = typeReader.injectMethods();
    this.preDestroyMethod = typeReader.preDestroyMethod();
    if (injectFields.isEmpty() && injectMethods.isEmpty()) {
      APContext.logError(
          beanType,
          "@InjectorTarget type %s must have at least one @Inject field or method to inject",
          type);
    }
  }

  TypeElement beanType() {
    return beanType;
  }

  List<FieldReader> injectFields() {
    return injectFields;
  }

  List<MethodReader> injectMethods() {
    return injectMethods;
  }

  Element preDestroyMethod() {
    return preDestroyMethod;
  }

  boolean needsTryForMethodInjection() {
    for (MethodReader injectMethod : injectMethods) {
      if (injectMethod.methodThrows()) {
        return true;
      }
    }
    return false;
  }

  String shortName() {
    return Util.shortName(type);
  }

  String packageName() {
    return APContext.elements().getPackageOf(beanType).getQualifiedName().toString();
  }

  private Set<String> importTypesFor() {
    importTypes.add(type);
    typeReader.extraImports(importTypes);
    injectFields.forEach(r -> r.addImports(importTypes));
    injectMethods.forEach(r -> r.addImports(importTypes));
    importTypes.add(Constants.GENERATED);
    importTypes.add("io.avaje.inject.spi.Injector");
    if (preDestroyMethod != null) {
      importTypes.add("java.util.Set");
      importTypes.add("java.util.Collections");
      importTypes.add("java.util.IdentityHashMap");
      importTypes.add("io.avaje.inject.PreDestroy");
    }
    return importTypes.forImport();
  }

  void writeImports(Append writer, String pkgName) {
    for (String importType : importTypesFor()) {
      if (Util.validImportType(importType, pkgName)) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }
}
