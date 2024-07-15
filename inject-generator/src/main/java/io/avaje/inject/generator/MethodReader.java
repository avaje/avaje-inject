package io.avaje.inject.generator;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.avaje.inject.generator.ProcessingContext.asElement;
import static io.avaje.inject.generator.Constants.*;

final class MethodReader {

  private static final String CODE_COMMENT_BUILD_FACTORYBEAN = "  /**\n   * Create and register %s via factory bean method %s#%s().\n   */";

  private final ExecutableElement element;
  private final String factoryType;
  private final String methodName;
  private final boolean prototype;
  private final boolean primary;
  private final boolean secondary;
  private final boolean lazy;
  private final String returnTypeRaw;
  private final UType genericType;
  private final String shortName;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final String factoryShortName;
  private final String initMethod;
  private final String destroyMethod;
  private final Integer destroyPriority;
  private final boolean beanCloseable;
  private final String name;
  private final TypeReader typeReader;
  private final boolean optionalType;
  private final boolean multiRegister;
  private final BeanConditions conditions = new BeanConditions();
  private MethodParam observeParameter;

  MethodReader(ExecutableElement element, TypeElement beanType, ImportTypeMap importTypes) {
    this(element, beanType, null, null, importTypes);
  }

  MethodReader(ExecutableElement element, TypeElement beanType, BeanPrism bean, String qualifierName, ImportTypeMap importTypes) {
    this.element = element;
    if (bean != null) {
      prototype = PrototypePrism.isPresent(element);
      primary = PrimaryPrism.isPresent(element);
      secondary = SecondaryPrism.isPresent(element);
      lazy = LazyPrism.isPresent(element) || LazyPrism.isPresent(element.getEnclosingElement());
      conditions.readAll(element);
    } else {
      prototype = false;
      primary = false;
      secondary = false;
      lazy = false;
    }
    this.methodName = element.getSimpleName().toString();
    TypeMirror returnMirror = element.getReturnType();
    UType uType = UType.parse(returnMirror);
    String raw = returnMirror.toString();
    if (Util.isOptional(raw)) {
      optionalType = true;
      uType = uType.param0();
      returnTypeRaw = Util.trimWildcard(uType.full());
      multiRegister = false;
    } else if (raw.startsWith("java.util.List")) {
      optionalType = false;
      uType = uType.param0();
      returnTypeRaw = Util.trimWildcard(uType.full());
      multiRegister = true;
    } else {
      optionalType = false;
      returnTypeRaw = Util.trimWildcard(raw);
      multiRegister = false;
    }
    this.genericType = uType;
    String mainType = genericType.mainType();
    this.shortName = Util.shortName(mainType);
    this.factoryType = beanType.getQualifiedName().toString();
    this.factoryShortName = Util.shortName(factoryType);
    this.isVoid = Util.isVoid(mainType);
    String initMethod = (bean == null) ? null : bean.initMethod();
    String destroyMethod = (bean == null) ? null : bean.destroyMethod();
    this.destroyPriority = (bean == null) ? null : bean.destroyPriority();
    this.beanCloseable = (bean != null) && bean.autoCloseable();
    // for multiRegister we desire a qualifier name such that builder.isAddBeanFor() uses it and allows
    // other beans of the same type to also register, otherwise it defaults to slightly confusing behaviour
    this.name = multiRegister && qualifierName == null ? "multi" : qualifierName;
    TypeElement returnElement = multiRegister ? APContext.typeElement(uType.mainType()) : asElement(returnMirror);
    if (returnElement == null) {
      this.typeReader = null;
      this.initMethod = initMethod;
      this.destroyMethod = destroyMethod;
    } else {
      this.typeReader = new TypeReader(genericType, returnElement, importTypes);
      typeReader.process();
      MethodLifecycleReader lifecycleReader = new MethodLifecycleReader(returnElement, initMethod, destroyMethod);
      this.initMethod = lifecycleReader.initMethod();
      this.destroyMethod = lifecycleReader.destroyMethod();
    }
    if (lazy && prototype) {
      APContext.logError("Cannot use both @Lazy and @Prototype");
    }
  }

  @Override
  public String toString() {
    return "MethodReader{" +
      "element=" + element +
      ", params=" + params +
      '}';
  }

  void addDependsOnGeneric(Set<UType> set) {
    for (MethodParam param : params) {
      param.addDependsOnGeneric(set);
    }
    if (genericType.isGeneric()) {
      set.add(genericType);
    }
    if (typeReader != null) {
      set.addAll(typeReader.genericTypes());
    }
  }

  MethodReader read() {
    List<? extends VariableElement> ps = element.getParameters();
    for (VariableElement p : ps) {
      params.add(new MethodParam(p));
    }
    observeParameter = params.stream().filter(MethodParam::observeEvent).findFirst().orElse(null);
    return this;
  }

  String name() {
    return methodName;
  }

  List<MethodParam> params() {
    return params;
  }

  MetaData createMeta() {
    MetaData metaData = new MetaData(returnTypeRaw, name);
    metaData.setMethod(fullBuildMethod());

    List<String> dependsOn = new ArrayList<>(params.size() + 1);
    dependsOn.add(factoryType);

    conditions.requireTypes.stream()
      .map(t -> CONDITIONAL_DEPENDENCY + t)
      .forEach(dependsOn::add);
    conditions.missingTypes.stream()
      .filter(t -> !t.equals(returnTypeRaw))
      .map(t -> CONDITIONAL_DEPENDENCY + t)
      .forEach(dependsOn::add);

    for (final MethodParam param : params) {
      var dep = Util.addQualifierSuffix(param.named, Util.trimWildcard(param.paramType));
      dependsOn.add(dep);
    }
    metaData.setDependsOn(dependsOn, name);
    metaData.setProvides(
      typeReader == null
        ? Collections.emptyList()
        : Util.addQualifierSuffix(typeReader.provides(), name));
    metaData.setAutoProvides(
      typeReader == null
        ? List.of()
        : Util.addQualifierSuffix(typeReader.autoProvides(), name));
    metaData.setProvidesAspect(typeReader == null ? "" : typeReader.providesAspect());
    return metaData;
  }

  private String fullBuildMethod() {
    return factoryType + Constants.DI + ".build_" + element.getSimpleName().toString();
  }

  void builderGetFactory(Append writer, boolean factoryHasConditions) {
    if (factoryHasConditions) {
      writer.append("      var factory = builder.getNullable(%s.class);", factoryShortName).eol();
      writer.append("      if (factory == null) return;").eol();
    } else {
      writer.append("      var factory = builder.get(%s.class);", factoryShortName).eol();
    }
  }

  void builderBuildBean(Append writer) {
    writer.indent("      ");
    if (isVoid) {
      writer.append("factory.%s(", methodName);
    } else {
      String beanName = optionalType ? "optionalBean" : "bean";
      writer.append("var %s = factory.%s(", beanName, methodName);
    }
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).builderGetDependency(writer, "builder");
    }
    writer.append(");").eol();
  }

  void builderAddBeanProvider(Append writer) {
    if (isVoid) {
      APContext.logError("Error - void @Prototype method ?", element);
      return;
    }
    if (optionalType) {
      APContext.logError("Error - Optional type with @Prototype method is not supported", element);
      return;
    }

    if (multiRegister) {
      APContext.logError("Error - List<Provider<>> type with @Prototype method is not supported", element);
      return;
    }

    String indent = "    ";
    writer.indent(indent).append("  builder");
    if (prototype) {
      writer.append(".asPrototype()");
    } else if(secondary) {
      writer.append(".asSecondary()");
    }

    writer.indent(".registerProvider(() -> {").eol();

    startTry(writer, "  ");
    writer.indent(indent).append("  return ");
    writer.append("factory.%s(", methodName);
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).builderGetDependency(writer, "builder");
    }
    writer.append(");").eol();
    endTry(writer, "  ");
    writer.indent(indent).append("  });").eol();
    writer.indent(indent).append("}").eol();
  }

  void builderBuildAddBean(Append writer) {
    if (!isVoid) {
      if (optionalType) {
        writer.append("      if (optionalBean.isPresent()) {").eol();
        writer.append("        var bean = optionalBean.get();").eol();
      }
      String indent = optionalType ? "        " : "      ";
      writer.indent(indent);
      var hasLifecycleMethods = hasLifecycleMethods();

      if (hasLifecycleMethods && multiRegister) {
        writer.append("bean.stream()").eol().indent(indent).append("    .map(");
      } else if (hasLifecycleMethods) {
        writer.append("var $bean = ");
      } else if (multiRegister) {
        writer.append("bean.forEach(");
      }

      writer.append("builder");
      if (primary) {
        writer.append(".asPrimary()");
      } else if (secondary) {
        writer.append(".asSecondary()");
      } else if (prototype) {
        writer.append(".asPrototype()");
      }

      var lineEnd = hasLifecycleMethods ? "" : ";";
      if (Util.isProvider(returnTypeRaw)) {
        var providerRegister = multiRegister ? "::registerProvider)" + lineEnd : ".registerProvider(bean);";
        writer.append(providerRegister).eol();
      } else {
        var beanRegister = multiRegister ? "::register)" + lineEnd : ".register(bean);";
        writer.append(beanRegister).eol();
      }

      final var hasInitMethod = notEmpty(initMethod);
      if (hasInitMethod) {
        var addPostConstruct =
          multiRegister
            ? "    .peek(b -> builder.addPostConstruct(b::%s))"
            : "builder.addPostConstruct($bean::%s);";
        writer.indent(indent).append(addPostConstruct, initMethod).eol();
      }

      var priority = destroyPriority == null || destroyPriority == 1000 ? "" : ", " + destroyPriority;
      if (notEmpty(destroyMethod)) {
        var addPreDestroy =
          multiRegister
            ? "    .forEach(b -> builder.addPreDestroy(b::%s%s));"
            : "builder.addPreDestroy($bean::%s%s);";
        writer.indent(indent).append(addPreDestroy, destroyMethod, priority).eol();

      } else if (typeReader != null && typeReader.isClosable()) {
        var addPreDestroy =
          multiRegister
            ? "    .forEach(b -> builder.addPreDestroy(b::close%s));"
            : "builder.addPreDestroy($bean::close%s);";
        writer.indent(indent).append(addPreDestroy, priority).eol();

      } else if (beanCloseable) {
        var addAutoClosable =
          multiRegister
            ? "    .forEach(builder::addAutoClosable);"
            : "builder.addAutoClosable(bean);";
        writer.indent(indent).append(addAutoClosable).eol();

      } else if (multiRegister && hasInitMethod) {
        writer.indent(indent).append("    .forEach(x -> {});").eol();
      }

      if (optionalType) {
        writer.append("      }").eol();
      }
    }
  }

  private boolean hasLifecycleMethods() {
    return notEmpty(initMethod) || notEmpty(destroyMethod) || (typeReader != null && typeReader.isClosable() || beanCloseable);
  }

  private boolean notEmpty(String value) {
    return value != null && !value.isEmpty();
  }

  void addImports(ImportTypeMap importTypes) {
    for (MethodParam param : params) {
      param.addImports(importTypes);
    }
    // TYPE_ generic types are fully qualified
    if (optionalType) {
      importTypes.add(Constants.OPTIONAL);
    }
    conditions.addImports(importTypes);
  }

  Set<UType> genericTypes() {
    return typeReader == null ? Collections.emptySet() : typeReader.genericTypes();
  }

  void buildConditional(Append writer) {
    new ConditionalWriter(writer, conditions).write();
  }

  void buildAddFor(Append writer) {
    writer.append("    if (builder.isAddBeanFor(");
    if (isVoid) {
      writer.append("Void.class");
    } else {
      if (name != null && !name.isEmpty()) {
        writer.append("\"%s\", ", name);
      }
      if (typeReader != null) {
        writer.append(typeReader.typesRegister());
      }
    }
    writer.append(")) {").eol();
  }

  boolean methodThrows() {
    return !element.getThrownTypes().isEmpty();
  }

  void startTry(Append writer) {
    startTry(writer, "");
  }

  void startTry(Append writer, String indent) {
    if (methodThrows()) {
      writer.append(indent).append("      try {").eol();
      writer.setExtraIndent("  " + indent);
    }
  }

  void endTry(Append writer) {
    endTry(writer, "");
  }

  void endTry(Append writer, String indent) {
    if (methodThrows()) {
      writer.setExtraIndent(null);
      writer.append(indent).append("      } catch (Throwable e) {").eol();
      writer.append(indent).append("        throw new RuntimeException(\"Error during wiring\", e);").eol();
      writer.append(indent).append("      }").eol();
    }
  }

  /**
   * Check for request scoped dependency.
   */
  void checkRequest(BeanRequestParams requestParams) {
    for (MethodParam param : params) {
      param.checkRequest(requestParams);
    }
  }

  /**
   * Generate code for dependency inject for BeanFactory.
   */
  void writeRequestDependency(Append writer) {
    for (MethodParam param : params) {
      param.writeRequestDependency(writer);
    }
  }

  /**
   * Generate code constructor arguments.
   */
  void writeRequestConstructor(Append writer) {
    for (MethodParam param : params) {
      param.writeRequestConstructor(writer);
    }
  }

  void commentBuildMethod(Append writer) {
    writer.append(CODE_COMMENT_BUILD_FACTORYBEAN, shortName, factoryShortName, methodName).eol();
  }

  boolean isProtoType() {
    return prototype && !Util.isProvider(returnTypeRaw);
  }

  boolean isLazy() {
    return lazy;
  }

  boolean isUseProviderForSecondary() {
    return secondary && !optionalType && !Util.isProvider(returnTypeRaw);
  }

  boolean isPublic() {
    return element.getModifiers().contains(Modifier.PUBLIC);
  }

  boolean isNotPrivate() {
    return !element.getModifiers().contains(Modifier.PRIVATE);
  }

  void writeConstructorParams(Append writer) {
    for (MethodParam param : params) {
      writer.append(", ");
      param.writeMethodParam(writer);
    }
  }

  void writeConstructorInit(Append writer) {
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).writeConstructorInit(writer);
    }
  }

  void removeFromProvides(List<UType> provides) {
    for (MethodParam param : params) {
      param.removeFromProvides(provides);
    }
  }

  ExecutableElement element() {
    return element;
  }

  MethodParam observeParam() {
    return observeParameter;
  }

  static class MethodParam {

    private final VariableElement element;
    private final String named;
    private final UtilType utilType;
    private final String paramType;
    private final UType genericType;
    private final UType fullUType;
    private final boolean nullable;
    private final String simpleName;
    private boolean requestParam;
    private String requestParamName;
    private final boolean isBeanMap;
    private final boolean isAssisted;
    private final boolean isObserveEvent;

    MethodParam(VariableElement param) {
      this.element = param;
      this.simpleName = param.getSimpleName().toString();
      this.named = Util.getNamed(param);
      this.nullable = Util.isNullable(param);
      this.isBeanMap = QualifiedMapPrism.isPresent(param);
      this.utilType = Util.determineType(param.asType(), isBeanMap);
      this.paramType = utilType.rawType(isBeanMap);
      this.genericType = utilType.toUType();
      this.fullUType = UType.parse(param.asType());
      this.isAssisted = AssistedPrism.isPresent(param);
      this.isObserveEvent = ObservesPrism.isPresent(param);

      if (nullable || param.asType().toString().startsWith("java.util.Optional<")) {
        ProcessingContext.addOptionalType(paramType, named);
      }

      if (fullUType.fullWithoutAnnotations().startsWith("io.avaje.inject.events.Event")) {
        EventPublisherWriter.write(param);
      }
    }

    @Override
    public String toString() {
      return "MethodParam{" + fullUType + '}';
    }

    void addDependsOnGeneric(Set<UType> set) {
      if (genericType.isGeneric() && !Util.isProvider(genericType.mainType())) {
        set.add(genericType);
      }
    }

    void builderGetDependency(Append writer, String builderName) {
      writer.append(builderName).append(".").append(utilType.getMethod(nullable, isBeanMap));
      if (!genericType.isGeneric() || genericType.param0().kind() == TypeKind.WILDCARD) {
        writer.append(Util.shortName(genericType.mainType())).append(".class");
      } else {
        writer.append("TYPE_").append(Util.shortName(genericType).replace(".", "_"));
      }
      if (named != null && !named.isEmpty()) {
        writer.append(",\"").append(named).append("\"");
      } else if (!isGenericParam() && utilType.allowsNamedQualifier()) {
        // implied qualifier name, leading '!' means implied
        writer.append(",\"!");
        final String shortName = Util.shortName(paramType);
        if (simpleName.endsWith(shortName)) {
          writer.append(simpleName, 0, simpleName.length() - shortName.length());
        } else {
          writer.append(simpleName);
        }
        writer.append("\"");
      }
      writer.append(")");
    }

    String simpleName() {
      return simpleName;
    }

    boolean isProvider() {
      return Util.isProvider(paramType);
    }

    boolean isGenericType() {
      return genericType.isGeneric();
    }

    boolean isGenericParam() {
      return isGenericType() && !isProvider();
    }

    Dependency dependsOn() {
      return new Dependency(paramType, named, utilType.isCollection());
    }

    void addImports(ImportTypeMap importTypes) {
      if (isObserveEvent) {
        importTypes.add("java.util.function.Consumer");
        importTypes.add("io.avaje.inject.events.Observer");
        importTypes.add("io.avaje.inject.events.ObserverManager");
      }
      importTypes.addAll(fullUType.importTypes());
      Util.getNullableAnnotation(element).map(Object::toString).ifPresent(importTypes::add);
    }

    void checkRequest(BeanRequestParams requestParams) {
      requestParam = requestParams.check(paramType);
      if (requestParam) {
        requestParamName = requestParams.argumentName(paramType);
      }
    }

    void writeRequestDependency(Append writer) {
      if (!requestParam) {
        requestParamName = writer.nextName(Util.trimmedName(genericType));
        writer.append("  @Inject").eol();
        writer.append("  ");
        writer.append(genericType.shortWithoutAnnotations());
        writer.append(" %s;", requestParamName).eol().eol();
      }
    }

    void writeRequestConstructor(Append writer) {
      writer.commaAppend(requestParamName);
    }

    void writeMethodParam(Append writer) {
      if (nullable) {
        writer.append("@Nullable ");
      }
      if (genericType.isGeneric()) {
        writer.append(genericType.shortWithoutAnnotations());
      } else {
        writer.append(Util.shortName(genericType.mainType()));
      }
      writer.append(" ").append(simpleName);
    }

    void writeMethodParamAspect(Append writer) {
      if (fullUType.isGeneric()) {
        writer.append(fullUType.shortWithoutAnnotations());
      } else {
        writer.append(Util.shortName(fullUType.mainType()));
      }
      writer.append(" ").append(simpleName);
    }

    void writeMethodParamTypeAspect(Append writer) {
      writer.append(Util.shortName(fullUType.mainType()));
    }

    void writeConstructorInit(Append writer) {
      writer.append(simpleName);
    }

    void removeFromProvides(List<UType> provides) {
      provides.remove(genericType);
    }

    boolean assisted() {
      return isAssisted;
    }

    boolean observeEvent() {
      return isObserveEvent;
    }

    Element element() {
      return element;
    }

    UType getFullUType() {
      return fullUType;
    }

    String qualifier() {
      return named != null ? named : "";
    }
  }

}
