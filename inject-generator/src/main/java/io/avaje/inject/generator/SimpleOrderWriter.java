package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.ProcessingContext.createMetaInfWriterFor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.tools.FileObject;

/** Write the source code for the factory. */
final class SimpleOrderWriter {
  private final String modulePackage;
  private final String shortName;
  private final String fullName;
  private final Set<String> ordering;

  private Append writer;

  SimpleOrderWriter(Set<String> orderedModules, ScopeInfo scopeInfo) {
    this.ordering = orderedModules;
    this.modulePackage = scopeInfo.modulePackage();
    this.shortName = "CompiledOrder";
    this.fullName = modulePackage + "." + shortName;
    ProcessingContext.setOrderFQN(fullName);
  }

  void write() throws IOException {
    writer = new Append(APContext.createSourceFile(fullName).openWriter());
    writePackage();
    writeStartClass();
    writeBuildMethods();
    writer.close();
    writeServicesFile();
  }

  private void writeServicesFile() {
    try {
      FileObject jfo = createMetaInfWriterFor("META-INF/services/io.avaje.inject.spi.ModuleOrdering");
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

  private void writePackage() {
    writer.append("package %s;", modulePackage).eol().eol();
    writer
        .append(
            "import static java.util.Map.entry;\n"
                + "import static java.util.List.of;\n"
                + "\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "import java.util.Set;\n"
                + "import io.avaje.inject.spi.ModuleOrdering;"
                + "import io.avaje.inject.spi.Module;")
        .eol();

    writer.eol();
  }

  private void writeStartClass() {

    writer.append("public final class %s implements ModuleOrdering {", shortName).eol().eol();

    writer.append("  private final Module[] sortedModules = new Module[%s];", ordering.size()).eol();
    writer.append("  private static final Map<String, Integer> INDEXES =").eol();
    writer.append("      Map.ofEntries(");
    var count = 0;
    for (String moduleName : ordering) {
      if (count > 0) {
        writer.append(",");
      }
      writer.eol().append("          entry(\"%s\", %s)", moduleName, count);
      count++;
    }
    writer.append(");");
  }

  private void writeBuildMethods() {
    writer.append(
        "\n"
            + "  @Override\n"
            + "  public List<Module> factories() {\n"
            + "    return List.of(sortedModules);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public Set<String> orderModules() {\n"
            + "    return INDEXES.keySet();\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public void add(Module module) {\n"
            + "    final var index = INDEXES.get(module.getClass().getTypeName());\n"
            + "\n"
            + "    if (index != null) {\n"
            + "      sortedModules[index] = module;\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public boolean isEmpty() {\n"
            + "    return sortedModules.length == 0;\n"
            + "  }\n"
            + "}");
  }
}
