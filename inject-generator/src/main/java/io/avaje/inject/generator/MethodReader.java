package io.avaje.inject.generator;

import io.avaje.inject.Bean;
import javax.inject.Named;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MethodReader {

  private static final String CODE_COMMENT_LIFECYCLE = "  /**\n   * Lifecycle wrapper for %s.\n   */";
  private static final String CODE_COMMENT_BUILD_FACTORYBEAN = "  /**\n   * Create and register %s via factory bean method %s#%s().\n   */";

  private final ExecutableElement element;
  private final String factoryType;
  private final String methodName;
  private final String returnTypeRaw;
  private final String shortName;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final String factoryShortName;
  private final boolean isFactory;
  private final String initMethod;
  private final String destroyMethod;
  private final String name;
  private final TypeReader typeReader;
  private boolean optionalType;

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType) {
    this(context, element, beanType, null, null);
  }

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType, Bean bean, Named named) {
    this.isFactory = bean != null;
    this.element = element;
    this.methodName = element.getSimpleName().toString();
    TypeMirror returnMirror = element.getReturnType();
    String raw = returnMirror.toString();
    if (Util.isOptional(raw)) {
      optionalType = true;
      returnTypeRaw = Util.extractOptionalType(raw);
    } else {
      optionalType = false;
      returnTypeRaw = raw;
    }
    this.shortName = Util.shortName(returnTypeRaw);
    this.factoryType = beanType.getQualifiedName().toString();
    this.factoryShortName = Util.shortName(factoryType);
    this.isVoid = Util.isVoid(returnTypeRaw);
    String initMethod = (bean == null) ? null : bean.initMethod();
    String destroyMethod = (bean == null) ? null : bean.destroyMethod();
    this.name = (named == null) ? null : named.value().toLowerCase();
    TypeElement returnElement = context.element(returnTypeRaw);
    if (returnElement == null) {
      this.typeReader = null;
      this.initMethod = initMethod;
      this.destroyMethod = destroyMethod;
    } else {
      this.typeReader = new TypeReader(returnElement, context);
      typeReader.process();
      MethodLifecycleReader lifecycleReader = new MethodLifecycleReader(returnElement, initMethod, destroyMethod);
      this.initMethod = lifecycleReader.initMethod();
      this.destroyMethod = lifecycleReader.destroyMethod();
    }
  }

  void read() {
    List<? extends VariableElement> ps = element.getParameters();
    for (VariableElement p : ps) {
      params.add(new MethodParam(p));
    }
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
      dependsOn.add(param.paramType);
    }
    metaData.setDependsOn(dependsOn);
    metaData.setProvides(new ArrayList<>());
    return metaData;
  }

  private String fullBuildMethod() {
    return factoryType + "$di.build_" + element.getSimpleName().toString();
  }

  String builderGetFactory() {
    return String.format("      %s factory = builder.get(%s.class);", factoryShortName, factoryShortName);
  }

  String builderBuildBean() {
    StringBuilder sb = new StringBuilder();
    sb.append("      ");
    if (isVoid) {
      sb.append(String.format("factory.%s(", methodName));
    } else {
      String beanType = optionalType ? String.format("Optional<%s>", shortName) : shortName;
      String beanName = optionalType ? "optionalBean" : "bean";
      sb.append(beanType);
      sb.append(String.format(" %s = factory.%s(", beanName, methodName));
    }
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(params.get(i).builderGetDependency("builder"));
    }
    sb.append(");");
    return sb.toString();
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
      }
      if (optionalType) {
        writer.append("      }").eol();
      }
    }
  }

  private boolean hasLifecycleMethods() {
    return notEmpty(initMethod) || notEmpty(destroyMethod);
  }

  private boolean notEmpty(String value) {
    return value != null && !value.isEmpty();
  }

  void addImports(Set<String> importTypes) {
    for (MethodParam param : params) {
      param.addImports(importTypes);
    }
    if (isFactory) {
      importTypes.add(returnTypeRaw);
    }
    if (optionalType) {
      importTypes.add(Constants.OPTIONAL);
    }
    if (typeReader != null) {
      typeReader.addImports(importTypes);
    }
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

  public boolean isPublic() {
    return element.getModifiers().contains(Modifier.PUBLIC);
  }

  public boolean isNotPrivate() {
    return !element.getModifiers().contains(Modifier.PRIVATE);
  }

  static class MethodParam {

    private final String named;
    private final UtilType utilType;
    private final String paramType;
    private final GenericType genericType;
    private final boolean nullable;
    private int providerIndex;
    private boolean requestParam;
    private String requestParamName;

    MethodParam(VariableElement param) {
      this.named = Util.getNamed(param);
      this.nullable = Util.isNullable(param);
      this.utilType = Util.determineType(param.asType());
      this.paramType = utilType.rawType();
      this.genericType = GenericType.maybe(paramType);
    }

    String builderGetDependency(String builderName) {
      StringBuilder sb = new StringBuilder();
      if (isGenericParam()) {
        // passed as provider to build method
        sb.append("prov").append(providerIndex).append(".get(");
      } else {
        sb.append(builderName).append(".").append(utilType.getMethod(nullable));
      }
      if (genericType == null) {
        sb.append(Util.shortName(paramType)).append(".class");
      } else if (isProvider()) {
        sb.append(providerParam()).append(".class");
      }
      if (named != null) {
        sb.append(",\"").append(named).append("\"");
      }
      sb.append(")");
      return sb.toString();
    }

    private String providerParam() {
      return Util.shortName(Util.unwrapProvider(paramType));
    }

    boolean isProvider() {
      return Util.isProvider(paramType);
    }

    boolean isGenericType() {
      return genericType != null;
    }

    boolean isGenericParam() {
      return isGenericType() && !isProvider();
    }

    String getDependsOn() {
      return paramType;
    }

    void addImports(Set<String> importTypes) {
      if (genericType != null) {
        importTypes.add(Constants.PROVIDER);
        genericType.addImports(importTypes);
      } else {
        importTypes.add(paramType);
      }
    }

    void addProviderParam(Append writer, int providerIndex) {
      this.providerIndex = providerIndex;
      writer.append(", ");
      writer.append("Provider<");
      genericType.writeShort(writer);
      writer.append("> prov%s", providerIndex);
    }

    void checkRequest(BeanRequestParams requestParams) {
      requestParam = requestParams.check(paramType);
      if (requestParam) {
        requestParamName = requestParams.argumentName(paramType);
      }
    }

    void writeRequestDependency(Append writer) {
      if (!requestParam) {
        final String shortType = nm(paramType);
        requestParamName = writer.nextName(shortType.toLowerCase());
        writer.append("  @Inject").eol();
        writer.append("  %s %s;", shortType, requestParamName).eol().eol();
      }
    }

    void writeRequestConstructor(Append writer) {
      writer.commaAppend(requestParamName);
    }

    private String nm(String raw) {
      return Util.shortName(raw);
    }

  }
}
