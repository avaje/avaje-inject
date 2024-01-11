package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
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
  private final Optional<TypeElement> target;
  private final Optional<String> qualifier;

  AssistBeanReader(TypeElement beanType) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.typeReader = new TypeReader(GenericType.parse(type), beanType, importTypes, false);

    typeReader.process();
    this.requestParams = new BeanRequestParams(type);
    this.injectMethods = typeReader.injectMethods();
    this.injectFields = typeReader.injectFields();
    typeReader.preDestroyPriority();
    this.constructor = typeReader.constructor();
    this.target =
        AssistFactoryPrism.getOptionalOn(beanType)
            .map(AssistFactoryPrism::value)
            .filter(m -> !"java.lang.Void".equals(m.toString()))
            .map(APContext::asTypeElement);
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

    this.qualifier = NamedPrism.getOptionalOn(beanType).map(NamedPrism::value);

    target.ifPresent(this::validateTarget);
  }

  private void validateTarget(TypeElement t) {
    if (t.getKind() != ElementKind.INTERFACE || !t.getModifiers().contains(Modifier.ABSTRACT)) {
      APContext.logError(type, "@AssistFactory targets must be abstract");
    }
    var sb =
        new StringBuilder(
            String.format(
                "@AssistFactory targets for type %s must have a '%s create(",
                shortName(), shortName()));

    for (var iterator = assistedElements.iterator(); iterator.hasNext(); ) {
      var element = iterator.next();

      var typeName = UType.parse(element.asType());

      sb.append(
          String.format("%s %s", typeName.shortWithoutAnnotations(), element.getSimpleName()));
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }
    var errorMsg = sb.append(")' method.").toString();
    ElementFilter.methodsIn(t.getEnclosedElements()).stream()
        .filter(m -> m.getSimpleName().contentEquals("create"))
        .map(ExecutableElement::getParameters)
        .findAny()
        .ifPresentOrElse(
            params -> {
              var mismatched = params.size() != assistedElements.size();
              if (!mismatched) {
                for (int i = 0; i < assistedElements.size(); i++) {
                  var assistParam =
                      GenericType.parse(assistedElements.get(i).asType().toString()).shortName();
                  var targetParam =
                      GenericType.parse(params.get(i).asType().toString()).shortName();
                  if (!assistParam.equals(targetParam)) {
                    mismatched = true;
                  }
                }
              }
              if (mismatched) {
                APContext.logError(t, errorMsg);
              }
            },
            () -> APContext.logError(t, errorMsg));
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement beanType() {
    return beanType;
  }

  AssistBeanReader read() {
    if (constructor != null) {
      constructor.addImports(importTypes);
      constructor.checkRequest(requestParams);
    }
    for (FieldReader field : injectFields) {
      field.addImports(importTypes);
      field.checkRequest(requestParams);
    }
    for (MethodReader method : injectMethods) {
      method.addImports(importTypes);
      method.checkRequest(requestParams);
    }

    return this;
  }

  List<Dependency> dependsOn() {
    List<Dependency> list = new ArrayList<>();
    if (constructor != null) {
      for (MethodReader.MethodParam param : constructor.params()) {
        Dependency dependsOn = param.dependsOn();
        // BeanScope is always injectable with no impact on injection ordering
        if (!Constants.BEANSCOPE.equals(dependsOn.dependsOn())) {
          list.add(dependsOn);
        }
      }
    }
    return list;
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildRegister(Append writer) {
    writer.indent("    ");
    if (!isExtraInjectionRequired()) {
      writer.append("return bean;");
    }
  }

  private Set<String> importTypes() {
    importTypes.add("io.avaje.inject.AssistFactory");
    target.ifPresent(e -> importTypes.add(e.getQualifiedName().toString()));
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    qualifier.ifPresent(s -> importTypes.add(NamedPrism.PRISM_TYPE));
    typeReader.extraImports(importTypes);
    requestParams.addImports(importTypes);
    importTypes.add(Constants.GENERATED);

    constructor.addImports(importTypes);
    injectFields.forEach(r -> r.addImports(importTypes));
    injectMethods.forEach(r -> r.addImports(importTypes));
    return importTypes.forImport();
  }

  void writeImports(Append writer) {
    importTypes.add(Constants.COMPONENT);
    if (!injectFields().isEmpty()) {
      importTypes.add(Constants.TYPE);
    }
    for (String importType : importTypes()) {
      if (Util.validImportType(importType)) {
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

  void writeConstructorParams(Append writer) {
    if (constructor != null) {
      constructor.writeConstructorParams(writer);
    }
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
      return Util.packageOf(beanQualifiedName());
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

  public List<Element> assistElements() {
    return assistedElements;
  }

  public Optional<TypeElement> getTargetInterface() {
    return target;
  }

  public Optional<String> getQualifier() {
    return qualifier;
  }
}