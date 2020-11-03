package io.avaje.inject.generator;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * Write the source code for the factory.
 */
class SimpleFactoryWriter {

  private static final String CODE_COMMENT_FACTORY =
    "/**\n" +
    " * Generated source - Creates the BeanContext for the %s module.\n" +
    " * \n" +
    " * With JPMS Java module system this generated class should be explicitly\n" +
    " * registered in module-info via a <code>provides</code> clause like:\n" +
    " * \n" +
    " * <pre>{@code\n" +
    " * \n" +
    " *   module example {\n" +
    " *     requires io.avaje.inject;\n" +
    " *     \n" +
    " *     provides io.avaje.inject.spi.BeanContextFactory with %s._di$BeanContextFactory;\n" +
    " *     \n" +
    " *   }\n" +
    " * \n" +
    " * }</pre>\n" +
    " */";

  private static final String CODE_COMMENT_CREATE_CONTEXT =
    "  /**\n" +
      "   * Create and return the BeanContext.\n" +
      "   * <p>\n" +
      "   * Creates all the beans in order based on constuctor dependencies.\n" +
      "   * The beans are registered into the builder along with callbacks for\n" +
      "   * field injection, method injection and lifecycle support.\n" +
      "   * <p>\n" +
      "   * Ultimately the builder returns the BeanContext containing the beans.\n" +
      "   *\n" +
      "   * @param parent The parent context for multi-module wiring\n" +
      "   * @return The BeanContext containing the beans\n" +
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
    this.factoryShortName = "_di$BeanContextFactory";
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
    writer.append("  public BeanContext createContext(Builder parent) {").eol();
    writer.append("    builder.setParent(parent);").eol();
    writer.append("    // create beans in order based on constructor dependencies").eol();
    writer.append("    // i.e. \"provides\" followed by \"dependsOn\"").eol();
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append("    build_%s();", metaData.getBuildName()).eol();
    }
    writer.append("    return builder.build();").eol();
    writer.append("  }").eol();
    writer.eol();
  }

  private void writePackage() {

    writer.append("package %s;", factoryPackage).eol().eol();

    final String generated = context.getGeneratedAnnotation();
    if (generated != null) {
      writer.append("import %s;", generated).eol();
    }
    writer.append(Constants.IMPORT_BEANCONTEXT).eol();
    writer.append(Constants.IMPORT_CONTEXTMODULE).eol();
    writer.append(Constants.IMPORT_DEPENDENCYMETA).eol().eol();
    writer.append(Constants.IMPORT_BEANCONTEXTFACTORY).eol();
    writer.append(Constants.IMPORT_BUILDER).eol();
    writer.append(Constants.IMPORT_BUILDERFACTORY).eol();

    for (String type : ordering.getImportTypes()) {
      if (Util.validImportType(type)) {
        writer.append("import %s;", type).eol();
      }
    }
    writer.eol();
  }

  private void writeStartClass() {

    writer.append(CODE_COMMENT_FACTORY, context.contextName(), factoryPackage).eol();
    context.buildAtContextModule(writer);

    writer.append("public class %s implements BeanContextFactory {", factoryShortName).eol().eol();
    writer.append("  private final Builder builder;").eol().eol();

    writer.append("  public %s() {", factoryShortName).eol();
    context.buildNewBuilder(writer);
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String getName() {").eol();
    writer.append("    return builder.getName();").eol();
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String[] getProvides() {").eol();
    writer.append("    return builder.getProvides();").eol();
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public String[] getDependsOn() {").eol();
    writer.append("    return builder.getDependsOn();").eol();
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
