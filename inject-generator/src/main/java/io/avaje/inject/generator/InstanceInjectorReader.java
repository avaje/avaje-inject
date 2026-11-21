package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.InstanceInjector;

final class InstanceInjectorReader {

  private final TypeElement targetType;
  private final String targetName;
  private final String packageName;
  private final String generatedName;
  private final TypeReader typeReader;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final Optional<MethodReader> postConstructMethod;
  private final Element preDestroyMethod;
  private final Set<UType> genericTypes = new LinkedHashSet<>();
  private boolean written;

  InstanceInjectorReader(TypeElement targetType, String importedPackage) {
    this.targetType = targetType;
    this.targetName = targetType.getQualifiedName().toString();
    this.packageName =
        importedPackage == null
            ? APContext.elements().getPackageOf(targetType).getQualifiedName().toString()
            : importedPackage;
    this.generatedName = generatedName(targetType, importedPackage != null);

    if (targetType.getKind() != ElementKind.CLASS) {
      logError(targetType, "@InjectorTarget can only be used on classes");
    }
    if (!targetType.getTypeParameters().isEmpty()) {
      logError(targetType, "@InjectorTarget classes cannot declare type parameters");
    }
    if (importedPackage != null) {
      validateImportedAccess(targetType);
    }

    typeReader =
        new TypeReader(
            List.of(),
            UType.parse(targetType.asType()),
            targetType,
            importTypes,
            false);
    typeReader.process();
    injectFields = typeReader.injectFields();
    injectMethods = typeReader.injectMethods();
    postConstructMethod = typeReader.postConstructMethod();
    preDestroyMethod = typeReader.preDestroyMethod();

    if (injectFields.isEmpty() && injectMethods.isEmpty()) {
      logError(
          targetType,
          "@InjectorTarget type %s must have at least one @Inject field or method",
          targetName);
    }
    for (FieldReader field : injectFields) {
      field.addImports(importTypes);
      validateMemberAccess(field.element(), importedPackage != null);
      final UType type = UType.parse(field.element().asType());
      validateDependencyType(field.element(), type);
      field.addDependsOnGeneric(genericTypes);
    }
    for (MethodReader method : injectMethods) {
      method.addImports(importTypes);
      validateMemberAccess(method.element(), importedPackage != null);
      method.params().forEach(param -> validateDependencyType(param.element(), param.getFullUType()));
      method.addDependsOnGeneric(genericTypes);
    }
    postConstructMethod.ifPresent(
        method -> {
          method.addImports(importTypes);
          validateMemberAccess(method.element(), importedPackage != null);
          method.params().forEach(param -> validateDependencyType(param.element(), param.getFullUType()));
          method.addDependsOnGeneric(genericTypes);
        });
    if (preDestroyMethod != null) {
      validateMemberAccess(preDestroyMethod, importedPackage != null);
    }
    typeReader.genericTypes().forEach(genericTypes::add);
    typeReader.extraImports(importTypes);

    if (!genericTypes.isEmpty()) {
      importTypes.add("java.lang.reflect.Type");
      importTypes.add("io.avaje.inject.spi.GenericType");
      genericTypes.forEach(type -> importTypes.addAll(type.importTypes()));
    }
    importTypes.add(targetName);
    importTypes.add(BeanScope.class.getName());
    importTypes.add(Builder.class.getName());
    importTypes.add(InstanceInjector.class.getName());
    importTypes.add("io.avaje.inject.spi.Generated");
  }

  private void validateDependencyType(Element element, UType type) {
    if (containsTypeVariable(type)) {
      logError(element, "@InjectorTarget injection dependencies cannot use type variables: %s", type.full());
    }
  }

  private boolean containsTypeVariable(UType type) {
    return type.kind() == javax.lang.model.type.TypeKind.TYPEVAR
        || type.componentTypes().stream().anyMatch(this::containsTypeVariable);
  }

  private void validateImportedAccess(Element element) {
    if (!element.getModifiers().contains(Modifier.PUBLIC)) {
      logError(element, "Imported @InjectorTarget classes require public @Inject and lifecycle members");
    }
  }

  private void validateMemberAccess(Element element, boolean imported) {
    if (imported) {
      validateImportedAccess(element);
      return;
    }

    final Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.PUBLIC)
        || (!modifiers.contains(Modifier.PRIVATE)
            && packageName.equals(APContext.elements().getPackageOf(element).getQualifiedName().toString()))) {
      return;
    }
    logError(
        element,
        "@InjectorTarget cannot access injected member %s.%s from generated package %s; make the member public",
        element.getEnclosingElement(),
        element.getSimpleName(),
        packageName);
  }

  private String generatedName(TypeElement targetType, boolean imported) {
    final String typeName = targetType.getQualifiedName().toString();
    final String targetPackage =
        APContext.elements().getPackageOf(targetType).getQualifiedName().toString();
    final String relativeName =
        targetPackage.isEmpty()
            ? typeName
            : typeName.substring(targetPackage.length() + 1);
    final String baseName =
        imported
            ? typeName.replace('.', '_')
            : relativeName.replace('.', '$');
    return baseName + "$InstanceInjector";
  }

  TypeElement targetType() {
    return targetType;
  }

  String targetName() {
    return targetName;
  }

  String packageName() {
    return packageName;
  }

  String generatedName() {
    return generatedName;
  }

  String generatedTypeName() {
    return packageName.isEmpty() ? generatedName : packageName + "." + generatedName;
  }

  List<FieldReader> injectFields() {
    return injectFields;
  }

  List<MethodReader> injectMethods() {
    return injectMethods;
  }

  Optional<MethodReader> postConstructMethod() {
    return postConstructMethod;
  }

  List<Dependency> dependsOn() {
    final Map<String, Dependency> dependencies = new LinkedHashMap<>();
    for (FieldReader field : injectFields) {
      addDependency(dependencies, field.dependsOn());
    }
    for (MethodReader method : injectMethods) {
      method.params().forEach(param -> addDependency(dependencies, param.injectorDependsOn()));
    }
    postConstructMethod.ifPresent(
        method ->
            method.params().forEach(param -> addDependency(dependencies, param.injectorDependsOn())));
    return new ArrayList<>(dependencies.values());
  }

  private void addDependency(Map<String, Dependency> dependencies, Dependency dependency) {
    if (!Constants.BEANSCOPE.equals(dependency.dependsOn())) {
      dependencies.putIfAbsent(dependency.dependsOn(), dependency);
    }
  }

  Element preDestroyMethod() {
    return preDestroyMethod;
  }

  boolean hasPreDestroy() {
    return preDestroyMethod != null || typeReader.isClosable();
  }

  Integer preDestroyPriority() {
    return typeReader.preDestroyPriority();
  }

  Set<UType> genericTypes() {
    return genericTypes;
  }

  Set<String> imports() {
    return importTypes.forImport();
  }

  boolean isWritten() {
    return written;
  }

  void markWritten() {
    written = true;
  }
}
