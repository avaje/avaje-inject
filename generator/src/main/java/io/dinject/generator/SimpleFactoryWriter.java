package io.dinject.generator;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * Write the source code for the factory.
 */
class SimpleFactoryWriter {

  private final MetaDataOrdering ordering;
  private final ProcessingContext processingContext;

  private final String factoryPackage;
  private final String factoryShortName;
  private final String factoryFullName;

  private Append writer;

  SimpleFactoryWriter(MetaDataOrdering ordering, ProcessingContext processingContext) {
    this.ordering = ordering;
    this.processingContext = processingContext;

    String pkg = processingContext.getContextPackage();
    this.factoryPackage = (pkg != null) ? pkg : ordering.getTopPackage();
    processingContext.deriveContextName(factoryPackage);
    this.factoryShortName = "_di$Factory";
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
      FileObject jfo = processingContext.createMetaInfWriter();
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(factoryFullName);
        writer.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      processingContext.logError("Failed to write services file " + e.getMessage());
    }
  }

  private void writeBuildMethods() {
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append(metaData.buildMethod(ordering)).eol();
    }
  }

  private void writeCreateMethod() {

    writer.append("  @Override").eol();
    writer.append("  public BeanContext createContext(Builder parent) {").eol();
    writer.append("    builder.setParent(parent);").eol();
    for (MetaData metaData : ordering.getOrdered()) {
      writer.append("    build_%s();", metaData.getBuildName()).eol();
    }
    writer.append("    return builder.build();").eol();
    writer.append("  }").eol();
    writer.eol();
  }

  private void writePackage() {

    writer.append("package %s;", factoryPackage).eol().eol();

    final String generated = processingContext.getGeneratedAnnotation();
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
      if (Util.notVoid(type)) {
        writer.append("import %s;", type).eol();
      }
    }
    writer.eol();
  }

  private void writeStartClass() {

    processingContext.buildAtContextModule(writer);

    writer.append("public class %s implements BeanContextFactory {", factoryShortName).eol().eol();
    writer.append("  private final Builder builder;").eol().eol();

    writer.append("  public %s() {", factoryShortName).eol();
    processingContext.buildNewBuilder(writer);
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
    JavaFileObject jfo = processingContext.createWriter(factoryFullName);
    return jfo.openWriter();
  }
}
