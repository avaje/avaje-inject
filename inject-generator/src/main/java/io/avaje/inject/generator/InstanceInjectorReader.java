package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static javax.lang.model.type.TypeKind.TYPEVAR;

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

import io.avaje.inject.generator.MethodReader.MethodParam;

final class InstanceInjectorReader {

  private final TypeElement targetType;
  private final String targetName;
  private final String packageName;
  private final String generatedName;
  private final boolean enablePreDestroy;
  private final TypeReader typeReader;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final Optional<MethodReader> postConstructMethod;
  private final Element preDestroyMethod;
  private final Set<UType> genericTypes = new LinkedHashSet<>();
  private boolean written;

  InstanceInjectorReader(TypeElement targetType, String importedPackage, boolean enablePreDestroy) {
    final boolean imported = importedPackage != null;
    this.targetType = targetType;
    this.targetName = targetType.getQualifiedName().toString();
    this.packageName =
        imported
            ? importedPackage
            : APContext.elements().getPackageOf(targetType).getQualifiedName().toString();
    this.generatedName = generatedName(targetType, imported);
    this.enablePreDestroy = enablePreDestroy;

    if (targetType.getKind() != ElementKind.CLASS) {
      logError(targetType, "@InjectorTarget can only be used on classes");
    }
    if (!targetType.getTypeParameters().isEmpty()) {
      logError(targetType, "@InjectorTarget classes cannot declare type parameters");
    }
    if (imported && !targetType.getModifiers().contains(Modifier.PUBLIC)) {
      logError(targetType, "Imported @InjectorTarget class %s must be public", targetName);
    }

    typeReader =
        new TypeReader(List.of(), UType.parse(targetType.asType()), targetType, importTypes, false);
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
    boolean usesSet = false;
    for (FieldReader field : injectFields) {
      field.addImports(importTypes);
      validateMemberAccess(field.element(), imported);
      validateDependencyType(field.element(), field.type());
      if (field.isNamedQualifiedMap()) {
        logError(
            field.element(), "@InjectorTarget does not support a named @QualifiedMap dependency");
      }
      usesSet |= field.isSet();
      field.addDependsOnGeneric(genericTypes);
    }
    for (MethodReader method : injectMethods) {
      method.addImports(importTypes);
      validateMemberAccess(method.element(), imported);
      usesSet |= validateParams(method);
      method.addDependsOnGeneric(genericTypes);
    }
    if (postConstructMethod.isPresent()) {
      final MethodReader method = postConstructMethod.get();
      method.addImports(importTypes);
      validateMemberAccess(method.element(), imported);
      usesSet |= validateParams(method);
      method.addDependsOnGeneric(genericTypes);
    }
    if (preDestroyMethod != null) {
      if (enablePreDestroy) {
        validateMemberAccess(preDestroyMethod, imported);
      } else {
        logError(
            preDestroyMethod,
            "@InjectorTarget types do not support the @PreDestroy lifecycle by default, use %s(enablePreDestroy = true) to enable this."
                + " The BeanScope then holds a reference to each injected instance until the BeanScope is closed",
            imported ? "@InjectorTarget.Import" : "@InjectorTarget");
      }
    }
    if (usesSet) {
      importTypes.add("java.util.LinkedHashSet");
    }
    typeReader.genericTypes().forEach(genericTypes::add);
    typeReader.extraImports(importTypes);

    if (!genericTypes.isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
      genericTypes.forEach(type -> importTypes.addAll(type.importTypes()));
    }
    importTypes.add(targetName);
    importTypes.add(Constants.BEANSCOPE);
    importTypes.add(Constants.BUILDER);
    importTypes.add(Constants.INSTANCEINJECTOR);
    importTypes.add(Constants.GENERATED);
  }

  private boolean validateParams(MethodReader method) {
    boolean usesSet = false;
    for (MethodParam param : method.params()) {
      validateDependencyType(param.element(), param.getFullUType());
      if (param.isNamedQualifiedMap()) {
        logError(
            param.element(), "@InjectorTarget does not support a named @QualifiedMap dependency");
      }
      usesSet |= param.isSet();
    }
    return usesSet;
  }

  private void validateDependencyType(Element element, UType type) {
    if (containsTypeVariable(type)) {
      logError(
          element,
          "@InjectorTarget injection dependencies cannot use type variables: %s",
          type.full());
    }
  }

  private boolean containsTypeVariable(UType type) {
    return type.kind() == TYPEVAR
        || type.componentTypes().stream().anyMatch(this::containsTypeVariable);
  }

  private void validateMemberAccess(Element element, boolean imported) {
    if (imported) {
      if (!element.getModifiers().contains(Modifier.PUBLIC)) {
        logError(
            element,
            "Imported @InjectorTarget classes require public @Inject and lifecycle members");
      }
      return;
    }

    final Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.PUBLIC)
        || (!modifiers.contains(Modifier.PRIVATE)
            && packageName.equals(
                APContext.elements().getPackageOf(element).getQualifiedName().toString()))) {
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
        targetPackage.isEmpty() ? typeName : typeName.substring(targetPackage.length() + 1);
    final String baseName = imported ? typeName.replace('.', '_') : relativeName.replace('.', '$');
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
      addDependency(dependencies, field.injectorDependsOn());
    }
    for (MethodReader method : injectMethods) {
      method.params().forEach(param -> addDependency(dependencies, param.injectorDependsOn()));
    }
    postConstructMethod.ifPresent(
        method ->
            method
                .params()
                .forEach(param -> addDependency(dependencies, param.injectorDependsOn())));
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
    return enablePreDestroy && (preDestroyMethod != null || typeReader.isClosable());
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
