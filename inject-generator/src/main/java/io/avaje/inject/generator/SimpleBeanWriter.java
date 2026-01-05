package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static io.avaje.inject.generator.APContext.logError;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import io.avaje.inject.generator.MethodReader.MethodParam;

/**
 * Write the source code for the bean.
 */
final class SimpleBeanWriter {

  private final BeanReader beanReader;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private final String suffix;
  private final boolean proxied;
  private Append writer;
  private boolean beanScopeAvailable;

  SimpleBeanWriter(BeanReader beanReader) {
    this.beanReader = beanReader;
    this.packageName = beanReader.packageName();
    this.shortName = beanReader.shortName();
    this.suffix = beanReader.suffix();
    this.proxied = beanReader.isGenerateProxy();
    this.originName = packageName.isBlank() ? shortName : packageName + "." + shortName;
  }

  private Writer createFileWriter() throws IOException {
    String originName = this.originName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      originName = originName.replace(shortName, shortName.replace(".", "$"));
    }
    final JavaFileObject jfo = createSourceFile(originName + suffix);
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    if (isRequestScopedController()) {
      writeRequestCreate();
    } else {
      writeGenericTypeFields();
      writeStaticFactoryMethod();
      writeStaticFactoryBeanMethods();
    }
    writeClassEnd();
    writer.close();
  }

  private void writeGenericTypeFields() {
    // collect all types to prevent duplicates
    Set<UType> genericTypes =
      beanReader.allGenericTypes().stream()
        .map(Util::unwrapProvider)
        .filter(UType::isGeneric)
        .collect(toSet());

    if (!genericTypes.isEmpty()) {
      final Map<String, String> seenShortNames = new HashMap<>();
      final Set<String> writtenFields = new HashSet<>();

      for (final UType utype : genericTypes) {
        var type = Util.unwrapProvider(utype);
        final var fieldName = Util.shortName(type).replace(".", "_");
        final var components = type.componentTypes();
        if (components.size() == 1 && components.get(0).kind() == TypeKind.WILDCARD
            || components.stream().anyMatch(u -> u.kind() == TypeKind.TYPEVAR)
            || !writtenFields.add(fieldName)) {
          continue;
        }

        writer.append("  public static final Type TYPE_%s =", fieldName).eol()
          .append("      new GenericType<");
        writeGenericType(type, seenShortNames, writer);
        // use fully qualified types here rather than use type.writeShort(writer)
        writer.append(">(){}.type();").eol();
      }
      writer.eol();
    }
  }

  private void writeGenericType(UType type, Map<String, String> seenShortNames, Append writer) {
    final var typeShortName = Util.shortName(type.mainType());
    final var mainType = seenShortNames.computeIfAbsent(typeShortName, k -> type.mainType());
    if (type.isGeneric()) {
      final var shortName = Objects.equals(type.mainType(), mainType) ? typeShortName : type.mainType();
      writer.append(shortName);
      writer.append("<");
      boolean first = true;
      for (final var param : type.componentTypes()) {
        if (first) {
          first = false;
          writeGenericType(param, seenShortNames, writer);
          continue;
        }
        writer.append(", ");
        writeGenericType(param, seenShortNames, writer);
      }
      writer.append(">");
    } else {
      final var shortName = Objects.equals(type.mainType(), mainType) ? typeShortName : type.mainType();
      writer.append(shortName);
    }
  }

  private void writeRequestCreate() {
    beanReader.writeRequestCreate(writer);
  }

  private boolean isRequestScopedController() {
    return beanReader.isRequestScopedController();
  }

  private void writeStaticFactoryBeanMethods() {
    for (MethodReader factoryMethod : beanReader.factoryMethods()) {
      writeFactoryBeanMethod(factoryMethod);
    }
    beanReader.validate();
  }

  private void writeFactoryBeanMethod(MethodReader method) {
    method.commentBuildMethod(writer);
    writer.append("  public static void build_%s(%s builder) {", method.name(), beanReader.builderType()).eol();
    method.buildConditional(writer);
    method.buildAddFor(writer);
    method.builderGetFactory(writer, beanReader.hasConditions());
    if (method.isLazy() || method.isProtoType() || method.isUseProviderForSecondary()) {
      method.builderAddBeanProvider(writer);
    } else {
      method.startTry(writer);
      method.builderBuildBean(writer);
      method.builderBuildAddBean(writer);
      method.endTry(writer);
      writer.append("    }").eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeStaticFactoryMethod() {
    MethodReader constructor = beanReader.constructor();
    if (constructor == null) {
      logError(beanReader.beanType(), "Unable to determine constructor to use for %s? Add explicit @Inject to one of the constructors.", beanReader.beanType());
      return;
    }
    writeBuildMethodStart();
    if (proxied) {
      writer.append("    // this bean is proxied, see %s$Proxy$DI instead", shortName).eol();
    } else {
      writeAddFor(constructor);
    }
    writer.append("  }").eol().eol();
  }

  private void writeAddFor(MethodReader constructor) {
    beanReader.buildConditional(writer);
    beanReader.buildBeanAbsent(writer);
    if (beanReader.registerProvider()) {
      final String registerProvider;
      if (beanReader.proxyLazy()) {
        registerProvider = "registerLazy";
      } else if (beanReader.lazy()) {
        registerProvider = "registerProvider";
      } else {
        registerProvider = "asPrototype().registerProvider";
      }
      writer.start("var beanScope = builder.get(io.avaje.inject.BeanScope.class);").eol();
      this.beanScopeAvailable = true;
      writer.start("builder.");
      beanReader.writePriority(writer);
      writer.append("%s(() -> {", registerProvider).eol();
      writer.incIndent(); // indent for provider register
    }

    constructor.startTry(writer);
    writeCreateBean(constructor);
    beanReader.buildRegister(writer);
    beanReader.addLifecycleCallbacks(writer);
    if (beanReader.isExtraInjectionRequired()) {
      writeExtraInjection();
    }
    if (beanReader.registerProvider()) {
      beanReader.providerLifeCycle(writer);
      writeObserveMethods();
      writer.start("return bean;").eol();
      writer.decIndent(); // indent for provider register
      if (!constructor.methodThrows()) {
        writer.start("}");
        writeLazyRegister();
        writer.append(");").eol();
      }
    } else {
      writeObserveMethods();
    }
    constructor.endTry(writer);

    if (beanReader.registerProvider() && constructor.methodThrows()) {
      writer.start("}");
      writeLazyRegister();
      writer.append(");").eol();
    }

    writer.append("    }").eol();
  }

  private void writeLazyRegister() {
    if (beanReader.proxyLazy()) {
      String shortNameLazyProxy = Util.shortNameLazyProxy(beanReader.lazyProxyType()) + "$Lazy";
      writer.append(", ");
      if (beanReader.lazyProxyType().getTypeParameters().isEmpty()) {
        writer.append("%s::new", shortNameLazyProxy);
      } else {
        writer.append("p -> new %s<>(p)", shortNameLazyProxy);
      }
    }
  }

  private void writeBuildMethodStart() {
    writer.append("  public static void build(%s builder) {", beanReader.builderType()).eol();
  }

  private void writeCreateBean(MethodReader constructor) {
    writer.start("var bean = new %s(", shortName);
    // add constructor dependencies
    writeMethodParams("builder", constructor);
  }

  private void writeExtraInjection() {
    if (!beanReader.registerProvider()) {
      writer.start(" builder.addInjector(b -> {").eol();
      writer.start("   // field and method injection").eol();
      writer.incIndent();
    }
    injectFields();
    injectMethods();
    if (!beanReader.registerProvider()) {
      writer.decIndent();
      writer.start("});").eol();
    }
  }

  private void writeObserveMethods() {
    final var bean = "bean";
    var scope = beanScopeAvailable ? "beanScope" : "builder";

    for (MethodReader methodReader : beanReader.observerMethods()) {
      var observeEvent = methodReader.observeParam();
      var observeUtype = observeEvent.getFullUType();
      final var shortWithoutAnnotations = observeUtype.shortWithoutAnnotations();
      var injectParams = methodReader.params().stream().skip(1).collect(toList());
      if (!beanScopeAvailable && !injectParams.isEmpty()) {
        writer.start("var beanScope = builder.get(io.avaje.inject.BeanScope.class);").eol();
        beanScopeAvailable = true;
        scope = "beanScope";
      }

      for (MethodParam param : injectParams) {
        if (Constants.BEANSCOPE.equals(param.getFullUType().fullWithoutAnnotations())) {
          continue;
        }

        writer.start("Supplier<%s> %s = () -> ",
          param.getFullUType().shortType(),
          methodReader.name() + "$" + param.simpleName());
        param.builderGetDependency(writer, scope);
        writer.append(";").eol();
      }

      writer.start("Consumer<%s> %s = ", shortWithoutAnnotations, methodReader.name());

      var observeTypeString =
        !observeUtype.isGeneric() || observeUtype.param0().kind() == TypeKind.WILDCARD
          ? Util.shortName(observeUtype.mainType()) + ".class"
          : "TYPE_" + Util.shortName(observeUtype).replace(".", "_");

      if (methodReader.params().size() == 1) {
        writer.append("%s::%s;", bean, methodReader.name());
      } else {
        var finalScope = scope;
        var injectParamNames = injectParams.stream()
          .map(p ->
            Constants.BEANSCOPE.equals(p.getFullUType().fullWithoutAnnotations())
              ? finalScope
              : methodReader.name() + "$" + p.simpleName() + ".get()")
          .collect(joining(", "));
        writer.append("e -> bean.%s(e, %s);", methodReader.name(), injectParamNames);
      }
      final var observesPrism = ObservesPrism.getInstanceOn(observeEvent.element());
      writer
          .eol()
          .start(scope)
          .eol()
          .start("    .get(ObserverManager.class)")
          .eol()
          .start("    .<%s>registerObserver(", shortWithoutAnnotations)
          .eol()
          .start(
              "        %s, new Observer<>(%s, %s, %s, \"%s\"));",
              observeTypeString,
              observesPrism.priority(),
              observesPrism.async(),
              methodReader.name(),
              observeEvent.qualifier())
          .eol();
    }
  }

  private void injectFields() {
    String bean = beanReader.registerProvider() ? "bean" : "$bean";
    String builder = beanReader.registerProvider() ? "beanScope" : "b";
    for (FieldReader fieldReader : beanReader.injectFields()) {
      String fieldName = fieldReader.fieldName();
      String getDependency = fieldReader.builderGetDependency(builder);
      writer.start("%s.%s = %s;", bean, fieldName, getDependency).eol();
    }
  }

  private void injectMethods() {
    final var needsTry = beanReader.needsTryForMethodInjection();
    final var bean = beanReader.registerProvider() ? "bean" : "$bean";
    final var builder = beanReader.registerProvider() ? "beanScope" : "b";
    if (needsTry) {
      writer.start("try {").eol().incIndent();
    }
    for (MethodReader methodReader : beanReader.injectMethods()) {
      writer.start("%s.%s(", bean, methodReader.name());
      writeMethodParams(builder, methodReader);
    }
    if (needsTry) {
      writer.decIndent();
      writer.start("} catch (Throwable e) {").eol();
      writer.start("  throw new RuntimeException(\"Error wiring method\", e);").eol();
      writer.start("}").eol();
    }
  }

  private void writeMethodParams(String builderRef, MethodReader methodReader) {
    List<MethodReader.MethodParam> methodParams = methodReader.params();
    for (int i = 0; i < methodParams.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      methodParams.get(i).builderGetDependency(writer, builderRef);
    }
    writer.append(");").eol();
  }

  private void writeImports() {
    beanReader.writeImports(writer, packageName);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    final var requestScopedController = beanReader.isRequestScopedController();
    writer.append(beanReader.generatedType()).append(Constants.AT_GENERATED_COMMENT).eol();
    if (requestScopedController) {
      writer.append(Constants.AT_SINGLETON).eol();
    }
    String shortName = this.shortName;
    if (beanReader.beanType().getNestingKind().isNested()) {
      shortName = shortName.replace(".", "$");
    }
    writer
      .append("public final %sclass ", requestScopedController ? "" : Util.valhalla())
      .append(shortName)
      .append(suffix)
      .append(" ");
    if (requestScopedController) {
      writer.append("implements ");
      beanReader.factoryInterface(writer);
    }
    writer.append(" {").eol().eol();
  }

  private void writePackage() {
    if (packageName != null && !packageName.isBlank()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
