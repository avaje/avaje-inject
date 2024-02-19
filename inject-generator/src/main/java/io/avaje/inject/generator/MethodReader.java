package io.avaje.inject.generator;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
  private final BeanConditions conditions = new BeanConditions();

  MethodReader(ExecutableElement element, TypeElement beanType, ImportTypeMap importTypes) {
    this(element, beanType, null, null, importTypes);
  }

  MethodReader(ExecutableElement element, TypeElement beanType, BeanPrism bean, String qualifierName, ImportTypeMap importTypes) {
    this.element = element;
    if (bean != null) {
      prototype = PrototypePrism.isPresent(element);
      primary = PrimaryPrism.isPresent(element);
      secondary = SecondaryPrism.isPresent(element);
      conditions.readAll(element);
    } else {
      prototype = false;
      primary = false;
      secondary = false;
    }
    this.methodName = element.getSimpleName().toString();
    TypeMirror returnMirror = element.getReturnType();
    UType uType = UType.parse(returnMirror);
    String raw = returnMirror.toString();
    if (Util.isOptional(raw)) {
      optionalType = true;
      returnTypeRaw = Util.trimWildcard(Util.extractOptionalType(raw));
      uType = uType.param0();
    } else {
      optionalType = false;
      returnTypeRaw = Util.trimWildcard(raw);
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
    this.name = qualifierName;
    TypeElement returnElement = asElement(returnMirror);
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
      dependsOn.add(Util.trimWildcard(param.paramType));
    }
    metaData.setDependsOn(dependsOn);
    metaData.setProvides(typeReader == null ? Collections.emptyList() : typeReader.provides());
    metaData.setAutoProvides(typeReader == null ? List.of() : typeReader.autoProvides());
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
      writer.append("Error - void @Prototype method ?").eol();
      return;
    }
    if (optionalType) {
      writer.append("Error - Optional type with @Prototype method is not supported").eol();
      return;
    }
    String indent = "    ";
    if (prototype) {
      writer.indent(indent).append("  builder.asPrototype().registerProvider(() -> {").eol();
    } else {
      writer.indent(indent).append("  builder.asSecondary().registerProvider(() -> {").eol();
    }
    writer.indent(indent).append("    return ");
    writer.append("factory.%s(", methodName);
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).builderGetDependency(writer, "builder");
    }
    writer.append(");").eol();
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
      if (hasLifecycleMethods()) {
        writer.append("var $bean = ");
      }
      writer.append("builder");
      if (primary) {
        writer.append(".asPrimary()");
      } else if (secondary) {
        writer.append(".asSecondary()");
      }
      writer.append(".register(bean);").eol();
      if (notEmpty(initMethod)) {
        writer.indent(indent).append("builder.addPostConstruct($bean::%s);", initMethod).eol();
      }
      var priority = destroyPriority == null || destroyPriority == 1000 ? "" : ", " + destroyPriority;
      if (notEmpty(destroyMethod)) {
        writer.indent(indent).append("builder.addPreDestroy($bean::%s%s);", destroyMethod, priority).eol();
      } else if (typeReader != null && typeReader.isClosable()) {
        writer.indent(indent).append("builder.addPreDestroy($bean::close%s);", priority).eol();
      } else if (beanCloseable) {
        writer.indent(indent).append("builder.addAutoClosable($bean);").eol();
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
    if (methodThrows()) {
      writer.append("      try {").eol();
      writer.setExtraIndent("  ");
    }
  }

  void endTry(Append writer) {
    if (methodThrows()) {
      writer.setExtraIndent(null);
      writer.append("      } catch (Throwable e) {").eol();
      writer.append("        throw new RuntimeException(\"Error during wiring\", e);").eol();
      writer.append("      }").eol();
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

  public void commentBuildMethod(Append writer) {
    writer.append(CODE_COMMENT_BUILD_FACTORYBEAN, shortName, factoryShortName, methodName).eol();
  }

  boolean isProtoType() {
    return prototype;
  }

  boolean isUseProviderForSecondary() {
    return secondary && !optionalType;
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

    MethodParam(VariableElement param) {
      this.element = param;
      this.simpleName = param.getSimpleName().toString();
      this.named = Util.getNamed(param);
      this.nullable = Util.isNullable(param);
      this.utilType = Util.determineType(param.asType());
      this.isBeanMap = QualifiedMapPrism.isPresent(param);
      this.paramType = utilType.rawType(isBeanMap);
      this.genericType = utilType.toUType();
      this.fullUType = UType.parse(param.asType());
      this.isAssisted = AssistedPrism.isPresent(param);

      if (nullable || param.asType().toString().startsWith("java.util.Optional<")) {
        ProcessingContext.addOptionalType(paramType);
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
      } else if (isProvider()) {
        writer.append(providerParam()).append(".class");
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

    private String providerParam() {
      return Util.shortName(Util.unwrapProvider(paramType));
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
      return new Dependency(paramType, utilType.isCollection());
    }

    void addImports(ImportTypeMap importTypes) {
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

    Element element() {
      return element;
    }
  }
}
