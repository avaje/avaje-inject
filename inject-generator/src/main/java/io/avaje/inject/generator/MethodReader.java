package io.avaje.inject.generator;

import io.avaje.inject.Bean;

import jakarta.inject.Named;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MethodReader {

  private static final String CODE_COMMENT_LIFECYCLE = "  /**\n   * Lifecycle wrapper for %s.\n   */";
  private static final String CODE_COMMENT_BUILD_FACTORYBEAN = "  /**\n   * Create and register %s via factory bean method %s#%s().\n   */";

  private final ProcessingContext context;
  private final ExecutableElement element;
  private final String factoryType;
  private final String methodName;
  private final TypeMirror returnType;
  private final String returnTypeRaw;
  private final String shortName;
  private final boolean isVoid;
  private final List<MethodParam> params = new ArrayList<>();
  private final List<String> interfaceTypes = new ArrayList<>();
  private final String factoryShortName;
  private final boolean isFactory;
  private final String initMethod;
  private final String destroyMethod;
  private final String name;
  private String addForType;
  private boolean beanLifeCycle;

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType) {
    this(context, element, beanType, null, null);
  }

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType, Bean bean, Named named) {
    this.isFactory = bean != null;
    this.context = context;
    this.element = element;
    this.methodName = element.getSimpleName().toString();
    this.returnType = element.getReturnType();
    this.returnTypeRaw = returnType.toString();
    this.shortName = Util.shortName(returnTypeRaw);
    this.factoryType = beanType.getQualifiedName().toString();
    this.factoryShortName = Util.shortName(factoryType);
    this.isVoid = Util.isVoid(returnTypeRaw);
    this.initMethod = (bean == null) ? null : bean.initMethod();
    this.destroyMethod = (bean == null) ? null : bean.destroyMethod();
    this.name = (named == null) ? null : named.value();
    initInterfaces();
  }

  String getName() {
    return methodName;
  }

  private void initInterfaces() {
    Element element = context.asElement(returnType);
    if (element instanceof TypeElement) {
      TypeElement te = (TypeElement) element;
      if (te.getKind() == ElementKind.INTERFACE) {
        interfaceTypes.add(te.getQualifiedName().toString());
      }
      for (TypeMirror anInterface : te.getInterfaces()) {
        if (Constants.isBeanLifecycle(anInterface.toString())) {
          // directly implements BeanLifecycle
          beanLifeCycle = true;
        } else {
          interfaceTypes.add(anInterface.toString());
        }
      }
      if (interfaceTypes.size() == 1) {
        addForType = interfaceTypes.get(0);
      }
    }
  }

  void read() {
    List<? extends VariableElement> ps = element.getParameters();
    for (VariableElement p : ps) {
      params.add(new MethodParam(p));
    }
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
    if (isVoid) {
      sb.append(String.format("      factory.%s(", methodName));
    } else {
      sb.append(String.format("      %s bean = factory.%s(", Util.shortName(returnTypeRaw), methodName));
    }
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(params.get(i).builderGetDependency("builder"));
    }
    sb.append(");");
    return sb.toString();
  }

  void builderBuildAddBean(Append writer) {
    if (!isVoid) {
      writer.append("      ");
      if (beanLifeCycle || hasLifecycleMethods()) {
        writer.append("%s $bean = ", shortName);
      }
      writer.append("builder.register(bean, ");
      if (name == null) {
        writer.append("null");
      } else {
        writer.append("\"%s\"", name);
      }
      for (String anInterface : interfaceTypes) {
        writer.append(", ").append(Util.shortName(anInterface)).append(".class");
      }
      writer.append(");").eol();

      if (beanLifeCycle) {
        writer.append("      builder.addLifecycle($bean);").eol();
      } else if (hasLifecycleMethods()) {
        writer.append("      builder.addLifecycle(new %s$lifecycle($bean));", shortName).eol();
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
    if (beanLifeCycle || hasLifecycleMethods()) {
      importTypes.add(Constants.BEAN_LIFECYCLE);
    }
    importTypes.addAll(interfaceTypes);
  }

  void buildAddFor(Append writer) {

    writer.append("    if (builder.isAddBeanFor(");
    if (addForType != null) {
      writer.append(addForType).append(".class, ");
    }
    if (isVoid) {
      writer.append("Void.class)) {").eol();
    } else {
      writer.append(shortName).append(".class)) {").eol();
    }
  }

  /**
   * Add a $lifecycle class for factory method bean that has initMethod or destroyMethod.
   */
  void buildLifecycleClass(Append writer) {
    if (!hasLifecycleMethods()) {
      return;
    }
    writer.append(CODE_COMMENT_LIFECYCLE, shortName).eol();
    writer.append("  static class %s$lifecycle implements BeanLifecycle {", shortName).eol().eol();
    writer.append("    final %s bean;", shortName).eol().eol();
    writer.append("    %s$lifecycle(%s bean) {", shortName, shortName).eol();
    writer.append("      this.bean = bean;").eol();
    writer.append("    }").eol().eol();

    writer.append("    @Override").eol();
    writer.append("    public void postConstruct() {").eol();
    if (notEmpty(initMethod)) {
      writer.append("      bean.%s();", initMethod).eol();
    } else {
      writer.append("      // do nothing ").eol();
    }
    writer.append("    }").eol().eol();

    writer.append("    @Override").eol();
    writer.append("    public void preDestroy() {").eol();
    if (notEmpty(destroyMethod)) {
      writer.append("      bean.%s();", destroyMethod).eol();
    } else {
      writer.append("      // do nothing ").eol();
    }
    writer.append("    }").eol();
    writer.append("  }").eol().eol();
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

  static class MethodParam {

    private final String named;
    private final UtilType utilType;
    private final String paramType;
    private final GenericType genericType;
    private int providerIndex;
    private boolean requestParam;
    private String requestParamName;

    MethodParam(VariableElement param) {
      this.named = Util.getNamed(param);
      this.utilType = Util.determineType(param.asType());
      this.paramType = utilType.rawType();
      this.genericType = GenericType.maybe(paramType);
    }

    String builderGetDependency(String builderName) {
      StringBuilder sb = new StringBuilder();
      if (genericType != null) {
        // passed as provider to build method
        sb.append("prov").append(providerIndex).append(".get(");
      } else {
        sb.append(builderName).append(".").append(utilType.getMethod());
      }
      if (genericType == null) {
        sb.append(Util.shortName(paramType)).append(".class");
      }
      if (named != null) {
        sb.append(",\"").append(named).append("\"");
      }
      sb.append(")");
      return sb.toString();
    }

    boolean isGenericType() {
      return genericType != null;
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
