package io.avaje.inject.generator;

import static io.avaje.inject.generator.ProcessingContext.asElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


final class MethodReader {

  private static final String CODE_COMMENT_BUILD_FACTORYBEAN = "  /**\n   * Create and register %s via factory bean method %s#%s().\n   */";

  private final ExecutableElement element;
  private final String factoryType;
  private final String methodName;
  private final boolean prototype;
  private final boolean primary;
  private final boolean secondary;
  private final String returnTypeRaw;
  private final GenericType genericType;
  private final String shortName;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final String factoryShortName;
  private final boolean isFactory;
  private final String initMethod;
  private final String destroyMethod;
  private final boolean beanCloseable;
  private final String name;
  private final TypeReader typeReader;
  private final boolean optionalType;

  MethodReader(ExecutableElement element, TypeElement beanType, ImportTypeMap importTypes) {
    this(element, beanType, null, null, importTypes);
  }

  MethodReader(ExecutableElement element, TypeElement beanType, BeanPrism bean, String qualifierName, ImportTypeMap importTypes) {
    this.isFactory = bean != null;
    this.element = element;
    if (isFactory) {
      prototype = PrototypePrism.isPresent(element);
      primary = PrimaryPrism.isPresent(element);
      secondary = SecondaryPrism.isPresent(element);
    } else {
      prototype = false;
      primary = false;
      secondary = false;
    }
    this.methodName = element.getSimpleName().toString();
    TypeMirror returnMirror = element.getReturnType();
    String raw = returnMirror.toString();
    if (Util.isOptional(raw)) {
      optionalType = true;
      returnTypeRaw = GenericType.trimWildcard(Util.extractOptionalType(raw));
    } else {
      optionalType = false;
      returnTypeRaw = GenericType.trimWildcard(raw);
    }
    this.genericType = GenericType.parse(returnTypeRaw);
    String topType = genericType.topType();
    this.shortName = Util.shortName(topType);
    this.factoryType = beanType.getQualifiedName().toString();
    this.factoryShortName = Util.shortName(factoryType);
    this.isVoid = Util.isVoid(topType);
    String initMethod = (bean == null) ? null : bean.initMethod();
    String destroyMethod = (bean == null) ? null : bean.destroyMethod();
    this.beanCloseable = (bean != null) && bean.autoCloseable();
    this.name = qualifierName;
    TypeElement returnElement = (TypeElement) asElement(returnMirror);
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

  void addDependsOnGeneric(Set<GenericType> set) {
    for (MethodParam param : params) {
      param.addDependsOnGeneric(set);
    }
    if (genericType.isGenericType()) {
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
    for (MethodParam param : params) {
      dependsOn.add(GenericType.trimWildcard(param.paramType));
    }
    metaData.setDependsOn(dependsOn);
    metaData.setProvides(typeReader == null ? Collections.emptyList() : typeReader.provides());
    metaData.setAutoProvides(typeReader == null ? null : typeReader.autoProvides());
    metaData.setProvidesAspect(typeReader == null ? "" : typeReader.providesAspect());
    return metaData;
  }

  private String fullBuildMethod() {
    return factoryType + Constants.DI + ".build_" + element.getSimpleName().toString();
  }

  String builderGetFactory() {
    return String.format("      var factory = builder.get(%s.class);", factoryShortName);
  }

  void builderBuildBean(Append writer) {
    writer.append("      ");
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
      params.get(i).builderGetDependency(writer, "builder", true);
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
      writer.append(indent).append("  builder.asPrototype().registerProvider(() -> {").eol();
    } else {
      writer.append(indent).append("  builder.asSecondary().registerProvider(() -> {").eol();
    }
    writer.append("%s    return ", indent);
    writer.append(String.format("factory.%s(", methodName));
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).builderGetDependency(writer, "builder", true);
    }
    writer.append(");").eol();
    writer.append(indent).append("  });").eol();
    writer.append(indent).append("}").eol();
  }

  void builderBuildAddBean(Append writer) {
    if (!isVoid) {
      String indent = optionalType ? "        " : "      ";
      if (optionalType) {
        writer.append("      if (optionalBean.isPresent()) {").eol();
        writer.append("        var bean = optionalBean.get();").eol();
      }
      writer.append(indent);
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
        writer.append(indent).append("builder.addPostConstruct($bean::%s);", initMethod).eol();
      }
      if (notEmpty(destroyMethod)) {
        writer.append(indent).append("builder.addPreDestroy($bean::%s);", destroyMethod).eol();
      } else if (typeReader != null && typeReader.isClosable()) {
        writer.append(indent).append("builder.addPreDestroy($bean::close);").eol();
      } else if (beanCloseable) {
        writer.append(indent).append("builder.addAutoClosable($bean);").eol();
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
  }

  Set<GenericType> genericTypes() {
    return typeReader == null ? Collections.emptySet() : typeReader.genericTypes();
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

  void removeFromProvides(List<String> provides) {
    for (MethodParam param : params) {
      param.removeFromProvides(provides);
    }
  }

  static class MethodParam {

    private final String named;
    private final UtilType utilType;
    private final String paramType;
    private final GenericType genericType;
    private final GenericType fullGenericType;
    private final boolean nullable;
    private final String simpleName;
    private boolean requestParam;
    private String requestParamName;
    private final boolean isBeanMap;

    MethodParam(VariableElement param) {
      this.simpleName = param.getSimpleName().toString();
      this.named = Util.getNamed(param);
      this.nullable = Util.isNullable(param);
      this.utilType = Util.determineType(param.asType());
      this.isBeanMap = QualifiedMapPrism.isPresent(param);
      this.paramType = utilType.rawType(isBeanMap);
      this.genericType = GenericType.parse(paramType);
      this.fullGenericType = GenericType.parse(utilType.full());
    }

    @Override
    public String toString() {
      return "MethodParam{" + fullGenericType + '}';
    }

    void addDependsOnGeneric(Set<GenericType> set) {
      if (genericType.isGenericType() && !genericType.isProviderType()) {
        set.add(genericType);
      }
    }

    void builderGetDependency(Append writer, String builderName, boolean forFactory) {
      writer.append(builderName).append(".").append(utilType.getMethod(nullable, isBeanMap));
      if (!genericType.isGenericType()) {
        writer.append(Util.shortName(genericType.topType())).append(".class");
      } else if (isProvider()) {
        writer.append(providerParam()).append(".class");
      } else {
        writer.append("TYPE_").append(genericType.shortName());
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
      return genericType.isGenericType();
    }

    boolean isGenericParam() {
      return isGenericType() && !isProvider();
    }

    Dependency dependsOn() {
      return new Dependency(paramType, utilType.isCollection());
    }

    void addImports(ImportTypeMap importTypes) {
      fullGenericType.addImports(importTypes);
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
        genericType.writeShort(writer);
        writer.append(" %s;", requestParamName).eol().eol();
      }
    }

    void writeRequestConstructor(Append writer) {
      writer.commaAppend(requestParamName);
    }

    void writeMethodParam(Append writer) {
      if (genericType.isGenericType()) {
        genericType.writeShort(writer);
      } else {
        writer.append(Util.shortName(genericType.topType()));
      }
      writer.append(" ").append(simpleName);
    }

    void writeMethodParamAspect(Append writer) {
      if (fullGenericType.isGenericType()) {
        fullGenericType.writeShort(writer);
      } else {
        writer.append(Util.shortName(fullGenericType.topType()));
      }
      writer.append(" ").append(simpleName);
    }

    void writeMethodParamTypeAspect(Append writer) {
      writer.append(Util.shortName(fullGenericType.topType()));
    }

    void writeConstructorInit(Append writer) {
      writer.append(simpleName);
    }

    void removeFromProvides(List<String> provides) {
      provides.remove(genericType.toString());
    }
  }
}
