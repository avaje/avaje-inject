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
class SimpleModuleWriter {

  private static final String CODE_COMMENT_FACTORY =
    "/**\n" +
      " * Generated source - avaje inject module for %s.\n" +
      " * \n" +
      " * With JPMS Java module system this generated class should be explicitly\n" +
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
      "   * Create the beans.\n" +
      "   * <p>\n" +
      "   * Creates all the beans in order based on constructor dependencies.\n" +
      "   * The beans are registered into the builder along with callbacks for\n" +
      "   * field injection, method injection and lifecycle support.\n" +
      "   * <p>\n" +
      "   */";

  private final ProcessingContext context;
  private final String modulePackage;
  private final String shortName;
  private final String fullName;
  private final ScopeInfo scopeInfo;

  private Append writer;

  SimpleModuleWriter(ProcessingContext context, ScopeInfo scopeInfo) {
    this.context = context;
    this.scopeInfo = scopeInfo;
    this.modulePackage = scopeInfo.modulePackage();
    this.shortName = scopeInfo.moduleShortName();
    this.fullName = scopeInfo.moduleFullName();
  }

  void write(boolean includeServicesFile) throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();
    writeCreateMethod();
    writeEndClass();
    writer.close();

    if (includeServicesFile) {
      writeServicesFile();
    }
  }

  private void writeServicesFile() {
    try {
      FileObject jfo = context.createMetaInfWriter();
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(fullName);
        writer.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      context.logError("Failed to write services file " + e.getMessage());
    }
  }

  private void writeCreateMethod() {
    writer.append(CODE_COMMENT_CREATE_CONTEXT).eol();
    writer.append("  @Override").eol();
    writer.append("  public void build(Builder builder) {").eol();
    writer.append("    new %sBeanFactory(builder).build();", scopeInfo.name()).eol();
    writer.append("  }").eol();
    writer.eol();
  }

  private void writePackage() {
    writer.append("package %s;", modulePackage).eol().eol();
    for (String type : factoryImportTypes()) {
      writer.append("import %s;", type).eol();
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
    writer.append(CODE_COMMENT_FACTORY, scopeInfo.name(), modulePackage, shortName).eol();
    scopeInfo.buildAtInjectModule(writer);

    String custom = scopeInfo.isMainScope() ? "" : ".Custom";
    writer.append("public class %s implements Module%s {", shortName, custom).eol().eol();
    scopeInfo.buildFields(writer);

    writer.append("  @Override").eol();
    writer.append("  public Class<?>[] provides() {").eol();
    writer.append("    return provides;").eol();
    writer.append("  }").eol().eol();

    writer.append("  @Override").eol();
    writer.append("  public Class<?>[] requires() {").eol();
    writer.append("    return requires;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeEndClass() {
    writer.append("}").eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(fullName);
    return jfo.openWriter();
  }

}
