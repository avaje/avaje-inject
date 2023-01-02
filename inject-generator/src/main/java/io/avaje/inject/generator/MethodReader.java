package io.avaje.inject.generator;

import io.avaje.inject.Bean;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class MethodReader {

  private static final String CODE_COMMENT_BUILD_FACTORYBEAN = "  /**\n   * Create and register %s via factory bean method %s#%s().\n   */";

  private final ExecutableElement element;
  private final String factoryType;
  private final String methodName;
  private final boolean prototype;
  private final String returnTypeRaw;
  private final GenericType genericType;
  private final String shortName;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final String factoryShortName;
  private final boolean isFactory;
  private final String initMethod;
  private final String destroyMethod;
  private final String name;
  private final TypeReader typeReader;
  private final boolean optionalType;

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType) {
    this(context, element, beanType, null, null, false);
  }

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType, Bean bean, String qualifierName, boolean prototype) {
    this.isFactory = bean != null;
    this.prototype = prototype;
    this.element = element;
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
    this.name = qualifierName;
    TypeElement returnElement = (TypeElement)context.asElement(returnMirror);
    if (returnElement == null) {
      this.typeReader = null;
      this.initMethod = initMethod;
      this.destroyMethod = destroyMethod;
    } else {
      this.typeReader = new TypeReader(genericType, returnElement, context);
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
      set.addAll(typeReader.getGenericTypes());
    }
  }

  MethodReader read() {
    List<? extends VariableElement> ps = element.getParameters();
    for (VariableElement p : ps) {
      params.add(new MethodParam(p));
    }
    return this;
  }

  String getName() {
    return methodName;
  }

  List<MethodParam> getParams() {
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
    metaData.setProvides(typeReader == null ? Collections.emptyList() : typeReader.getProvides());
    return metaData;
  }

  private String fullBuildMethod() {
    return factoryType + Constants.DI + ".build_" + element.getSimpleName().toString();
  }

  String builderGetFactory() {
    return String.format("      %s factory = builder.get(%s.class);", factoryShortName, factoryShortName);
  }

  void builderBuildBean(Append writer) {
    writer.append("      ");
    if (isVoid) {
      writer.append(String.format("factory.%s(", methodName));
    } else {
      String beanType = optionalType ? String.format("Optional<%s>", shortName) : shortName;
      String beanName = optionalType ? "optionalBean" : "bean";
      writer.append(beanType);
      writer.append(String.format(" %s = factory.%s(", beanName, methodName));
    }
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      params.get(i).builderGetDependency(writer, "builder", true);
    }
    writer.append(");").eol();
  }

  public void builderAddProtoBean(Append writer) {
    if (isVoid) {
      writer.append("Error - void @Prototype method ?").eol();
      return;
    }
    if (optionalType) {
      writer.append("Error - Optional type with @Prototype method is not supported").eol();
      return;
    }
    String indent = "    ";
    writer.append(indent).append("  // prototype scope bean method").eol();
    writer.append(indent).append("  builder.registerProvider(() -> {").eol();
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
        writer.append("        %s bean = optionalBean.get();", shortName).eol();
      }
      writer.append(indent);
      if (hasLifecycleMethods()) {
        writer.append("%s $bean = ", shortName);
      }
      writer.append("builder.register(bean);").eol();
      if (notEmpty(initMethod)) {
        writer.append(indent).append("builder.addPostConstruct($bean::%s);", initMethod).eol();
      }
      if (notEmpty(destroyMethod)) {
        writer.append(indent).append("builder.addPreDestroy($bean::%s);", destroyMethod).eol();
      } else if (typeReader != null && typeReader.isClosable()) {
        writer.append(indent).append("builder.addPreDestroy($bean::close);", destroyMethod).eol();
      }
      if (optionalType) {
        writer.append("      }").eol();
      }
    }
  }

  private boolean hasLifecycleMethods() {
    return notEmpty(initMethod) || notEmpty(destroyMethod) || (typeReader != null && typeReader.isClosable());
  }

  private boolean notEmpty(String value) {
    return value != null && !value.isEmpty();
  }

  void addImports(Set<String> importTypes) {
    for (MethodParam param : params) {
      param.addImports(importTypes);
    }
    if (isFactory) {
      genericType.addImports(importTypes);
    }
    if (optionalType) {
      importTypes.add(Constants.OPTIONAL);
    }
    if (typeReader != null) {
      typeReader.addImports(importTypes);
    }
  }

  Set<GenericType> getGenericTypes() {
    return typeReader == null ? Collections.emptySet() : typeReader.getGenericTypes();
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
        writer.append(typeReader.getTypesRegister());
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
    private final boolean nullable;
    private final String simpleName;
    private boolean requestParam;
    private String requestParamName;

    MethodParam(VariableElement param) {
      this.simpleName = param.getSimpleName().toString();
      this.named = Util.getNamed(param);
      this.nullable = Util.isNullable(param);
      this.utilType = Util.determineType(param.asType());
      this.paramType = utilType.rawType();
      this.genericType = GenericType.parse(paramType);
      if (nullable || param.asType().toString().startsWith("java.util.Optional<"))
        ProcessingContext.getOptionalTypes().add(paramType);
    }

    @Override
    public String toString() {
      return "MethodParam{" +
        "genericType=" + genericType +
        '}';
    }

    void addDependsOnGeneric(Set<GenericType> set) {
      if (genericType.isGenericType() && !genericType.isProviderType()) {
        set.add(genericType);
      }
    }

    void builderGetDependency(Append writer, String builderName, boolean forFactory) {
      writer.append(builderName).append(".").append(utilType.getMethod(nullable));
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

    Dependency getDependsOn() {
      return new Dependency(paramType, utilType.isCollection());
    }

    void addImports(Set<String> importTypes) {
      if (genericType.isGenericType()) {
        importTypes.add(Constants.PROVIDER);
        genericType.addImports(importTypes);
      } else {
        genericType.addImports(importTypes);
      }
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

    void writeMethodParamType(Append writer) {
      writer.append(Util.shortName(genericType.topType()));
    }

    void writeConstructorInit(Append writer) {
      writer.append(simpleName);
    }

    void removeFromProvides(List<String> provides) {
      provides.remove(genericType.toString());
    }
  }
}
