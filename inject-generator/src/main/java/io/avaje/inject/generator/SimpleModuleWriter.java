package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.typeElement;
import static io.avaje.inject.generator.ProcessingContext.createMetaInfWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

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
      " *     provides io.avaje.inject.spi.Module with %s.%s;\n" +
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

  private Append writer;

  SimpleModuleWriter(MetaDataOrdering ordering, ScopeInfo scopeInfo) {
    this.ordering = ordering;
    this.scopeInfo = scopeInfo;
    this.modulePackage = scopeInfo.modulePackage();
    this.shortName = scopeInfo.moduleShortName();
    this.fullName = scopeInfo.moduleFullName();
  }

  void write(ScopeInfo.Type scopeType) throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();
    writeProvides();
    writeClassesMethod();
    writeBuildMethod();
    writeBuildMethods();
    writeEndClass();
    writer.close();
    if (scopeType != ScopeInfo.Type.CUSTOM) {
      writeServicesFile(scopeType);
    }
    if (!ordering.ordered().isEmpty()) {
      ProcessingContext.validateModule(fullName);
    }
  }

  private void writeServicesFile(ScopeInfo.Type scopeType) {
    try {
      FileObject jfo = createMetaInfWriter(scopeType);
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(fullName);
        writer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      logError("Failed to write services file " + e.getMessage());
    }
  }

  private void writeProvides() {
    final Set<String> autoProvidesAspects = new TreeSet<>();
    final Set<String> autoProvides = new TreeSet<>();

    for (MetaData metaData : ordering.ordered()) {
      final String aspect = metaData.providesAspect();
      if (aspect != null && !aspect.isEmpty()) {
        autoProvidesAspects.add(aspect);
      }
      final String forExternal = metaData.autoProvides();
      if (forExternal != null && !forExternal.isEmpty() && !forExternal.contains("<")) {
        autoProvides.add(forExternal);
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
  }

  private void writeClassesMethod() {
    Set<String> allClasses = distinctPublicClasses();
    writer.append("  @Override").eol();
    writer.append("  public Class<?>[] classes() {").eol();
    writer.append("    return new Class<?>[]{").eol();
    for (String rawType : new TreeSet<>(allClasses)) {
      writer.append("      %s.class,", rawType).eol();
    }
    writer.append("    };").eol();
    writer.append("  }").eol().eol();
  }

  /**
   * Return the distinct set of public classes that are dependency types.
   */
  private Set<String> distinctPublicClasses() {
    Set<String> publicClasses = new LinkedHashSet<>();
    for (MetaData metaData : ordering.ordered()) {
      String rawType = metaData.type();
      if (!"void".equals(rawType)) {
        String type = UType.parse(typeElement(rawType).asType()).mainType();
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
    writer.append("    this.builder = builder;").eol();
    writer.append("    // create beans in order based on constructor dependencies").eol();
    writer.append("    // i.e. \"provides\" followed by \"dependsOn\"").eol();
    for (MetaData metaData : ordering.ordered()) {
      if (!metaData.isGenerateProxy()) {
        writer.append("    build_%s();", metaData.buildName()).eol();
      }
    }
    writer.append("  }").eol();
    writer.eol();
  }

  private void writeBuildMethods() {
    for (MetaData metaData : ordering.ordered()) {
      metaData.buildMethod(writer);
    }
  }

  private void writePackage() {
    writer.append("package %s;", modulePackage).eol().eol();
    for (String type : factoryImportTypes()) {
      writer.append("import %s;", type).eol();
    }
    for (String type : scopeInfo.initModuleDependencies(ordering.importTypes())) {
      if (Util.validImportType(type)) {
        writer.append("import %s;", type).eol();
      }
    }
    writer.eol();
  }

  private Set<String> factoryImportTypes() {
    Set<String> importTypes = new TreeSet<>();
    importTypes.add(Constants.GENERATED);
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
    writer.append("public final class %s implements %s {", shortName, interfaceType).eol().eol();
    writer.append("  private Builder builder;").eol().eol();
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
