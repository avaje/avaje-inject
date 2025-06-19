package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.typeElement;
import static io.avaje.inject.generator.ProcessingContext.allScopes;
import static io.avaje.inject.generator.ProcessingContext.createMetaInfWriterFor;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import io.avaje.inject.generator.ScopeInfo.Type;

/**
 * Write the source code for the factory.
 */
final class SimpleModuleWriter {

  private static final String CODE_COMMENT_FACTORY =
    "/**\n" +
      " * Avaje Inject module for %s.\n" +
      " * \n" +
      " * When using the Java module system, this generated class should be explicitly\n" +
      " * registered in module-info via a <code>provides</code> clause like:\n" +
      " * \n" +
      " * <pre>{@code\n" +
      " * \n" +
      " *   module example {\n" +
      " *     requires io.avaje.inject;\n" +
      " *     \n" +
      " *     provides io.avaje.inject.spi.InjectExtension with %s.%s;\n" +
      " *     \n" +
      " *   }\n" +
      " * \n" +
      " * }</pre>\n" +
      " */";

  private static final String CODE_COMMENT_CREATE_CONTEXT =
    "  /**\n" +
      "   * Creates all the beans in order based on constructor dependencies.\n" +
      "   * The beans are registered into the builder along with callbacks for\n" +
      "   * field/method injection, and lifecycle support.\n" +
      "   */";

  private final String modulePackage;
  private final String shortName;
  private final String fullName;
  private final ScopeInfo scopeInfo;
  private final MetaDataOrdering ordering;
  private final Type scopeType;
  private final Set<String> duplicateTypes;

  private Append writer;

  SimpleModuleWriter(MetaDataOrdering ordering, ScopeInfo scopeInfo) {
    this.ordering = ordering;
    this.scopeInfo = scopeInfo;
    this.modulePackage = scopeInfo.modulePackage();
    this.shortName = scopeInfo.moduleShortName();
    this.fullName = scopeInfo.moduleFullName();
    this.scopeType = scopeInfo.type();

    final Set<String> seen = new HashSet<>();
    this.duplicateTypes =
      ordering.ordered().stream()
        .map(MetaData::type)
        .filter(t -> !seen.add(ProcessorUtils.shortType(t)))
        .flatMap(t -> Stream.of(t, t + "$DI"))
        .collect(toSet());
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();

    if (scopeType != ScopeInfo.Type.CUSTOM) {
      writeServicesFile(scopeType);
    } else {
      writeRequiredModules();
    }
    writeProvides();
    writeClassesMethod();
    writeBuildMethod();
    writeBuildMethods();
    writeEndClass();
    writer.close();
  }

  private void writeRequiredModules() {
    var directScopes =
      scopeInfo.requires().stream()
        .map(APContext::typeElement)
        .filter(ScopePrism::isPresent)
        .filter(e -> e.getKind() == ElementKind.ANNOTATION_TYPE)
        .map(TypeElement::getQualifiedName)
        .map(Object::toString)
        .map(allScopes()::get)
        .collect(toList());

    var dependentScopes =
      directScopes.stream()
        .filter(Objects::nonNull)
        .flatMap(scope -> scope.dependentScopes().stream())
        .collect(toList());

    if (hasExternalDependency(directScopes, dependentScopes)) {
      // don't write if dependent scopes have constructor params or external module
      return;
    }

    final Set<String> requiredModules = new LinkedHashSet<>();
    dependentScopes.stream()
      .map(ScopeInfo::moduleFullName)
      .filter(Objects::nonNull)
      .filter(Predicate.not(String::isBlank))
      .forEach(requiredModules::add);

    final Map<String, String> dependencies = new LinkedHashMap<>(scopeInfo.constructorDependencies());

    writer.append("  public static AvajeModule[] allRequiredModules(");
    boolean comma = false;
    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
      if (!comma) {
        comma = true;
      } else {
        writer.append(", ");
      }
      writer.append(entry.getKey()).append(" ").append(entry.getValue());
    }

    writer.append(") {").eol();
    writer.append("    return new AvajeModule[] {").eol();
    for (String rawType : requiredModules) {
      writer.append("      new %s(),", rawType).eol();
    }
    writer.append("      new %s(", shortName);
    writer.append(String.join(", ", scopeInfo.constructorDependencies().values()));
    writer.append(")").eol();
    writer.append("    };").eol();
    writer.append("  }").eol().eol();
  }

  private boolean hasExternalDependency(List<ScopeInfo> directScopes, List<ScopeInfo> dependentScopes) {
    if (directScopes.contains(null)) {
      return true;
    }
    for (var scope : dependentScopes) {
      if (scope.requires().stream().map(allScopes()::get).anyMatch(Objects::isNull)) {
        return true;
      }
    }
    return false;
  }

  private void writeServicesFile(ScopeInfo.Type scopeType) {
    try {
      if (scopeType == ScopeInfo.Type.DEFAULT) {
        ProcessingContext.addInjectSPI(fullName);
        return;
      }
      FileObject jfo = createMetaInfWriterFor(Constants.META_INF_TESTMODULE);
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(fullName);
        writer.close();
      }
    } catch (IOException e) {
      logError("Failed to write services file %s", e.getMessage());
    }
  }

  private void writeProvides() {
    final Set<String> autoProvidesAspects = new TreeSet<>();
    final Set<String> autoProvides = new TreeSet<>();

    if (scopeType == ScopeInfo.Type.CUSTOM) {
      autoProvides.add(scopeInfo.scopeAnnotationFQN());
      autoProvides.add(shortName);
    }

    for (MetaData metaData : ordering.ordered()) {
      final String aspect = metaData.providesAspect();
      if (aspect != null && !aspect.isEmpty()) {
        autoProvidesAspects.add(aspect);
      }
      final var forExternal = metaData.autoProvides();
      if (forExternal != null && !forExternal.isEmpty()) {
        autoProvides.addAll(forExternal);
      }
    }
    if (!autoProvides.isEmpty()) {
      scopeInfo.buildAutoProvides(writer, autoProvides);
    }
    if (!autoProvidesAspects.isEmpty()) {
      scopeInfo.buildAutoProvidesAspects(writer, autoProvidesAspects);
    }
    Set<String> autoRequires = ordering.autoRequires();
    if (!autoRequires.isEmpty()) {
      scopeInfo.buildAutoRequires(writer, autoRequires);
    }
    Set<String> autoRequiresAspects = ordering.autoRequiresAspects();
    if (!autoRequiresAspects.isEmpty()) {
      scopeInfo.buildAutoRequiresAspects(writer, autoRequiresAspects);
    }

    var requires = new ArrayList<>(scopeInfo.requires());
    var provides = new ArrayList<>(scopeInfo.provides());
    requires.addAll(autoRequires);
    autoRequiresAspects.stream().map(Util::wrapAspect).forEach(requires::add);
    provides.addAll(autoProvides);
    autoProvidesAspects.stream().map(Util::wrapAspect).forEach(provides::add);

    ProcessingContext.addModule(new ModuleData(fullName, provides, requires));
  }

  private void writeClassesMethod() {
    Set<String> allClasses = distinctPublicClasses();
    writer.append("  @Override").eol();
    writer.append("  public Class<?>[] classes() {").eol();
    writer.append("    return new Class<?>[] {").eol();
    for (String rawType : new TreeSet<>(allClasses)) {
      writer.append("      %s.class,", rawType).eol();
    }
    writer.append("    };").eol();
    writer.append("  }").eol().eol();
  }

  /** Return the distinct set of public classes that are dependency types. */
  private Set<String> distinctPublicClasses() {
    Set<String> publicClasses = new LinkedHashSet<>();
    for (MetaData metaData : ordering.ordered()) {
      String rawType = metaData.type();
      if (!"void".equals(rawType) && !ProcessorUtils.isPrimitive(rawType)) {

        String type = Util.trimGenerics(rawType);
        TypeElement element = typeElement(type);
        if (element != null && element.getModifiers().contains(Modifier.PUBLIC)) {
          publicClasses.add(type);
        }
      }
    }
    return publicClasses;
  }

  private void writeBuildMethod() {
    writer.append(CODE_COMMENT_CREATE_CONTEXT).eol();
    writer.append("  @Override").eol();
    writer.append("  public void build(Builder builder) {").eol();
    if (scopeInfo.addWithBeans()) {
      writeWithBeans();
    }
    writer.append("    // create beans in order based on constructor dependencies").eol();
    writer.append("    // i.e. \"provides\" followed by \"dependsOn\"").eol();
    for (MetaData metaData : ordering.ordered()) {
      if (!metaData.isGenerateProxy()) {
        writer.append("    build_%s(builder);", metaData.buildName()).eol();
      }
    }
    writer.append("  }").eol();
    writer.eol();
  }

  private void writeBuildMethods() {
    for (MetaData metaData : ordering.ordered()) {
      metaData.buildMethod(writer, duplicateTypes.contains(metaData.type()));
    }
  }

  private void writePackage() {
    writer.append("package %s;", modulePackage).eol().eol();
    for (String type : factoryImportTypes()) {
      writer.append("import %s;", type).eol();
    }

    for (String type : scopeInfo.initModuleDependencies(ordering.importTypes())) {
      if (!duplicateTypes.contains(type) && Util.validImportType(type, modulePackage)) {
        writer.append("import %s;", type).eol();
      }
    }
    writer.eol();
  }

  private Set<String> factoryImportTypes() {
    Set<String> importTypes = new TreeSet<>();
    importTypes.add(Constants.GENERATED);
    importTypes.add(Constants.GENERICTYPE);
    importTypes.add("java.lang.reflect.Type");
    importTypes.add(Constants.BEANSCOPE);
    importTypes.add(Constants.INJECTMODULE);
    importTypes.add(Constants.DEPENDENCYMETA);
    importTypes.add(Constants.MODULE);
    importTypes.add(Constants.BUILDER);
    return importTypes;
  }

  private void writeStartClass() {
    writer.append(CODE_COMMENT_FACTORY, scopeInfo.name(), modulePackage, shortName).eol();
    scopeInfo.buildAtInjectModule(writer);

    String interfaceType = scopeInfo.type().type();
    writer.append("public final %sclass %s implements %s {", Util.valhalla(), shortName, interfaceType).eol().eol();
    if (scopeInfo.addModuleConstructor()) {
      writeConstructor();
    }
    scopeInfo.buildProvides(writer);
  }

  private void writeWithBeans() {
    writer.append("    // register external dependencies").eol();
    final Map<String, String> dependencies = scopeInfo.constructorDependencies();
    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
      writer.append("    builder.withBean(%s.class, %s);", entry.getKey(), entry.getValue()).eol();
    }
  }

  private void writeConstructor() {
    final Map<String, String> dependencies = scopeInfo.constructorDependencies();
    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
      writer.append("  private %s %s;", entry.getKey(), entry.getValue()).eol();
    }
    writer.eol();
    writer.append("  /**").eol();
    writer.append("   * Create providing the external dependencies.").eol();
    writer.append("   */").eol();
    writer.append("  public %s(", shortName);

    boolean comma = false;
    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
      if (!comma) {
        comma = true;
      } else {
        writer.append(", ");
      }
      writer.append(entry.getKey()).append(" ").append(entry.getValue());
    }
    writer.append(") {", shortName).eol();
    for (Map.Entry<String, String> entry : dependencies.entrySet()) {
      writer.append("    this.%s = %s;", entry.getValue(), entry.getValue()).eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeEndClass() {
    writer.append("}").eol();
  }

  private Writer createFileWriter() throws IOException {
    return scopeInfo.moduleFile().openWriter();
  }

}
