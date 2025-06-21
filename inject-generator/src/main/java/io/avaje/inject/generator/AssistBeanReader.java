package io.avaje.inject.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import io.avaje.inject.generator.MethodReader.MethodParam;

final class AssistBeanReader {

  private final TypeElement beanType;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final List<Element> assistedElements = new ArrayList<>();

  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private final TypeElement targetType;
  private final String qualifierName;
  private final ExecutableElement factoryMethod;

  AssistBeanReader(TypeElement beanType) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.typeReader =
      new TypeReader(List.of(), UType.parse(beanType.asType()), beanType, importTypes, false);

    typeReader.process();
    qualifierName = typeReader.name();
    this.requestParams = new BeanRequestParams(type);
    this.injectMethods = typeReader.injectMethods();
    this.injectFields = typeReader.injectFields();
    this.constructor = typeReader.constructor();

    AssistFactoryPrism instanceOn = AssistFactoryPrism.getInstanceOn(beanType);
    var factoryType = APContext.asTypeElement(instanceOn.value());

    constructor.params().stream()
      .filter(MethodParam::assisted)
      .map(MethodParam::element)
      .forEach(assistedElements::add);
    injectFields.stream()
      .filter(FieldReader::assisted)
      .map(FieldReader::element)
      .forEach(assistedElements::add);
    injectMethods.stream()
      .map(MethodReader::params)
      .flatMap(List::stream)
      .filter(MethodParam::assisted)
      .map(MethodParam::element)
      .forEach(assistedElements::add);

    factoryMethod =
      ElementFilter.methodsIn(factoryType.getEnclosedElements()).stream()
        .filter(e -> e.getModifiers().contains(Modifier.ABSTRACT))
        .findFirst()
        .orElse(null);

    validateTarget(factoryType);
    this.targetType = factoryType;
  }

  private void validateTarget(TypeElement t) {
    var methods = ElementFilter.methodsIn(t.getEnclosedElements());
    if (!APContext.elements().isFunctionalInterface(t)) {
      if (!t.getModifiers().contains(Modifier.ABSTRACT)) {
        APContext.logError(type, "@AssistFactory targets must be abstract");
      } else if (checkAbstractMethodCount(methods)) {
        APContext.logError(type, "@AssistFactory targets must have only one abstract method");
      }
    }

    if (t.getTypeParameters().size() > 1) {
      APContext.logError(type, "@AssistFactory targets can't have more than one type parameter");
    }

    var sb = new StringBuilder(String.format("@AssistFactory targets for type %s must have an abstract method with form '%s <methodName>(", shortName(), shortName()));
    var assistNames = new ArrayList<String>();
    for (var iterator = assistedElements.iterator(); iterator.hasNext(); ) {
      var element = iterator.next();
      var typeName = UType.parse(element.asType());
      sb.append(String.format("%s %s", typeName.shortWithoutAnnotations(), element.getSimpleName()));
      if (iterator.hasNext()) {
        sb.append(", ");
      }
      assistNames.add(String.format("%s %s", typeName.shortWithoutAnnotations(), element.getSimpleName()));
    }
    var errorMsg = sb.append(")' method.").toString();

    Optional.ofNullable(factoryMethod).stream()
      .map(ExecutableElement::getParameters)
      .findAny()
      .ifPresentOrElse(params -> {
          var mismatched = params.size() != assistedElements.size();
          if (mismatched) {
            APContext.logError(t, errorMsg);
            return;
          }

          var paramTypes = params.stream()
            .map(v -> String.format("%s %s", UType.parse(v.asType()).shortWithoutAnnotations(), v.getSimpleName()))
            .collect(toSet());

          var missingParams = assistNames.stream().filter(not(paramTypes::contains)).collect(joining(", "));
          if (!missingParams.isBlank()) {
            APContext.logError(factoryMethod, "factory method missing required parameters: %s", missingParams);
          }
        },
        () -> APContext.logError(t, errorMsg));
  }

  private static boolean checkAbstractMethodCount(List<ExecutableElement> methods) {
    return methods.stream()
      .filter(e -> e.getModifiers().contains(Modifier.ABSTRACT))
      .count() != 1;
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement beanType() {
    return beanType;
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildRegister(Append writer) {
    if (!isExtraInjectionRequired()) {
      writer.indent("    return bean;");
    }
  }

  private Set<String> importTypes() {
    Optional.ofNullable(targetType).ifPresent(t -> importTypes.add(t.getQualifiedName().toString()));

    if (qualifierName != null) {
      importTypes.add(NamedPrism.PRISM_TYPE);
    }
    typeReader.extraImports(importTypes);
    requestParams.addImports(importTypes);
    importTypes.add(Constants.GENERATED);

    constructor.addImports(importTypes);
    injectFields.forEach(r -> r.addImports(importTypes));
    injectMethods.forEach(r -> r.addImports(importTypes));
    return importTypes.forImport();
  }

  void writeImports(Append writer, String pkgName) {
    importTypes.add(Constants.COMPONENT);
    if (!injectFields().isEmpty()) {
      importTypes.add(Constants.TYPE);
    }
    for (String importType : importTypes()) {
      if (Util.validImportType(importType, pkgName)) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }

  MethodReader constructor() {
    return constructor;
  }

  List<FieldReader> injectFields() {
    return typeReader.injectFields();
  }

  List<MethodReader> injectMethods() {
    return typeReader.injectMethods();
  }

  String shortName() {
    return Util.shortName(beanQualifiedName());
  }

  String packageName() {
    return beanPackageName();
  }

  private String beanPackageName() {
    if (beanType.getNestingKind().isNested()) {
      return Util.nestedPackageOf(beanQualifiedName());
    } else {
      return ProcessorUtils.packageOf(beanQualifiedName());
    }
  }

  private String beanQualifiedName() {
    return beanType.getQualifiedName().toString();
  }

  boolean needsTryForMethodInjection() {
    for (MethodReader injectMethod : injectMethods) {
      if (injectMethod.methodThrows()) {
        return true;
      }
    }
    return false;
  }

  List<Element> assistElements() {
    return assistedElements;
  }

  TypeElement targetInterface() {
    return targetType;
  }

  String qualifierName() {
    return qualifierName;
  }

  String factoryMethodName() {
    return factoryMethod != null ? factoryMethod.getSimpleName().toString() : "create";
  }

  boolean hasTargetFactory() {
    return factoryMethod != null;
  }

  List<? extends VariableElement> factoryMethodParams() {
    return factoryMethod.getParameters();
  }
}
