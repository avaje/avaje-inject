package io.avaje.inject.generator;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Write the source code for the factory.
 */
class SimpleFactoryWriter {

  private static final String CODE_COMMENT_FACTORY =
    "/**\n" +
      " * Generated source - Creates the beans for the %s module.\n" +
      " */";

  private static final String CODE_COMMENT_CREATE_CONTEXT =
    "  /**\n" +
      "   * Create the beans.\n" +
      "   * <p>\n" +
      "   * Creates all the beans in order based on constructor dependencies.\n" +
      "   * The beans are registered into the builder along with callbacks for\n" +
      "   * field injection, method injection and lifecycle support.\n" +
      "   * <p>\n" +
      "   */";

  private final MetaDataOrdering ordering;
  private final ProcessingContext context;
  private final String factoryPackage;
  private final String factoryShortName;
  private final String factoryFullName;
  private final ScopeInfo scopeInfo;

  private Append writer;

  SimpleFactoryWriter(MetaDataOrdering ordering, ProcessingContext context, ScopeInfo scopeInfo) {
    this.ordering = ordering;
    this.context = context;
    this.scopeInfo = scopeInfo;
    this.factoryPackage = ordering.getTopPackage();
    this.factoryShortName = scopeInfo.factoryShortName(factoryPackage);
    this.factoryFullName = factoryPackage + "." + factoryShortName;
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();

    writeBuildMethod();
    writeBuildMethods();

    writeEndClass();
    writer.close();
  }

  private void writeBuildMethods() {
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append(metaData.buildMethod(ordering)).eol();
    }
    for (MetaData metaData : ordering.getRequestScope()) {
      writer.append(metaData.buildMethod(ordering)).eol();
    }
  }

  private void writeBuildMethod() {
    writer.append(CODE_COMMENT_CREATE_CONTEXT).eol();

    writer.append("  void build() {").eol();
    writer.append("    // create beans in order based on constructor dependencies").eol();
    writer.append("    // i.e. \"provides\" followed by \"dependsOn\"").eol();
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append("    build_%s();", metaData.getBuildName()).eol();
    }
    final List<MetaData> requestScope = ordering.getRequestScope();
    if (!requestScope.isEmpty()) {
      writer.eol();
      writer.append("    // request scope providers").eol();
      for (MetaData metaData : requestScope) {
        writer.append("    build_%s();", metaData.getBuildName()).eol();
      }
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
    importTypes.add(Constants.INJECTMODULE);
    importTypes.add(Constants.DEPENDENCYMETA);
    importTypes.add(Constants.MODULE);
    importTypes.add(Constants.BUILDER);
    return importTypes;
  }

  private void writeStartClass() {
    writer.append(CODE_COMMENT_FACTORY, scopeInfo.name(), factoryPackage, factoryShortName).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("class %s {", factoryShortName).eol().eol();

    writer.append("  private final Builder builder;").eol().eol();
    writer.append("  %s(Builder builder) {", factoryShortName).eol();
    writer.append("    this.builder = builder;").eol();
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
