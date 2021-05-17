package io.avaje.inject.generator;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

/**
 * Write the source code for the factory.
 */
class SimpleFactoryWriter {

  private static final String CODE_COMMENT_FACTORY =
    "/**\n" +
      " * Generated source - Creates the BeanScope for the %s module.\n" +
      " * \n" +
      " * With JPMS Java module system this generated class should be explicitly\n" +
      " * registered in module-info via a <code>provides</code> clause like:\n" +
      " * \n" +
      " * <pre>{@code\n" +
      " * \n" +
      " *   module example {\n" +
      " *     requires io.avaje.inject;\n" +
      " *     \n" +
      " *     provides io.avaje.inject.spi.BeanScopeFactory with %s._DI$BeanScopeFactory;\n" +
      " *     \n" +
      " *   }\n" +
      " * \n" +
      " * }</pre>\n" +
      " */";

  private static final String CODE_COMMENT_CREATE_CONTEXT =
    "  /**\n" +
      "   * Create the beans.\n" +
      "   * <p>\n" +
      "   * Creates all the beans in order based on constuctor dependencies.\n" +
      "   * The beans are registered into the builder along with callbacks for\n" +
      "   * field injection, method injection and lifecycle support.\n" +
      "   * <p>\n" +
      "   */";

  private final MetaDataOrdering ordering;
  private final ProcessingContext context;
  private final String factoryPackage;
  private final String factoryShortName;
  private final String factoryFullName;

  private Append writer;

  SimpleFactoryWriter(MetaDataOrdering ordering, ProcessingContext context) {
    this.ordering = ordering;
    this.context = context;

    String pkg = context.getContextPackage();
    this.factoryPackage = (pkg != null) ? pkg : ordering.getTopPackage();
    context.deriveContextName(factoryPackage);
    this.factoryShortName = "_DI$BeanScopeFactory";
    this.factoryFullName = factoryPackage + "." + factoryShortName;
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();

    writeCreateMethod();
    writeBuildMethods();

    writeEndClass();
    writer.close();

    writeServicesFile();
  }

  private void writeServicesFile() {
    try {
      FileObject jfo = context.createMetaInfWriter();
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(factoryFullName);
        writer.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      context.logError("Failed to write services file " + e.getMessage());
    }
  }

  private void writeBuildMethods() {
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append(metaData.buildMethod(ordering)).eol();
    }
  }

  private void writeCreateMethod() {
    writer.append(CODE_COMMENT_CREATE_CONTEXT).eol();
    writer.append("  @Override").eol();
    writer.append("  public void build(Builder builder) {").eol();
    writer.append("    this.builder = builder;").eol();
    writer.append("    // create beans in order based on constructor dependencies").eol();
    writer.append("    // i.e. \"provides\" followed by \"dependsOn\"").eol();
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append("    build_%s();", metaData.getBuildName()).eol();
    }
    writer.append("  }").eol();
    writer.eol();
  }

  private void writePackage() {
    writer.append("package %s;", factoryPackage).eol().eol();
    for (String type : factoryImportTypes()) {
      writer.append("import %s;", type).eol();
    }
    for (String type : ordering.getImportTypes()) {
      if (Util.validImportType(type)) {
        writer.append("import %s;", type).eol();
      }
    }
    writer.eol();
  }

  private Set<String> factoryImportTypes() {
    Set<String> importTypes = new TreeSet<>();
    importTypes.add(Constants.GENERATED);
    importTypes.add(Constants.BEANCONTEXT);
    importTypes.add(Constants.CONTEXTMODULE);
    importTypes.add(Constants.DEPENDENCYMETA);
    importTypes.add(Constants.BEANSCOPEFACTORY);
    importTypes.add(Constants.BUILDER);
    return importTypes;
  }

  private void writeStartClass() {
    writer.append(CODE_COMMENT_FACTORY, context.contextName(), factoryPackage).eol();
    context.buildAtContextModule(writer);

    writer.append("public class %s implements BeanScopeFactory {", factoryShortName).eol().eol();
    writer.append("  private final String name;").eol();
    writer.append("  private final String[] provides;").eol();
    writer.append("  private final String[] dependsOn;").eol();
    writer.append("  private Builder builder;").eol().eol();

    writer.append("  public %s() {", factoryShortName).eol();
    context.buildNewBuilder(writer);
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String getName() {").eol();
    writer.append("    return name;").eol();
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String[] getProvides() {").eol();
    writer.append("    return provides;").eol();
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String[] getDependsOn() {").eol();
    writer.append("    return dependsOn;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeEndClass() {
    writer.append("}").eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(factoryFullName);
    return jfo.openWriter();
  }

}
