package io.avaje.inject.generator;

import java.io.IOException;
import java.util.Set;

/** Write the source code for the factory. */
final class SimpleOrderWriter {

  private final String modulePackage;
  private final String shortName;
  private final String fullName;
  private final Set<String> ordering;

  private Append writer;
  private int numberOfModules;

  SimpleOrderWriter(Set<String> orderedModules, ScopeInfo scopeInfo) {
    this.ordering = orderedModules;
    this.modulePackage = scopeInfo.modulePackage();
    this.shortName = "CompiledOrder";
    this.fullName = modulePackage.isBlank() ? shortName : modulePackage + "." + shortName;
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
    ProcessingContext.addInjectSPI(fullName);
  }

  private void writePackage() {
    if (!modulePackage.isBlank()) {
      writer.append("package %s;", modulePackage).eol().eol();
    }
    writer
      .append(
        "import java.util.List;\n"
          + "import java.util.Set;\n"
          + "import io.avaje.inject.spi.Generated;\n"
          + "import io.avaje.inject.spi.ModuleOrdering;\n"
          + "import io.avaje.inject.spi.AvajeModule;")
      .eol();

    writer.eol();
  }

  private void writeStartClass() {
    writer.append(
      "/**\n" +
        " * Ordering of modules based on module provides and requires dependencies.\n" +
        " * Refer to target/avaje-module-dependencies.csv for details.\n" +
        " */\n"
    );
    writer.append(Constants.AT_SUPPRESS_WARNINGS).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("public final %sclass %s implements ModuleOrdering {", Util.valhalla(), shortName).eol().eol();

    var size = ordering.size();
    this.numberOfModules = 0;
    if (size == 0) {
      writer.append("  private static final Set<String> MODULE_NAMES = Set.of();").eol();
    } else {
      writer.append("  private static final Set<String> MODULE_NAMES = Set.of(").eol();
      for (String moduleName : ordering) {
        writer.append("    \"%s\"", moduleName);
        if (++numberOfModules < size) {
          writer.append(",").eol();
        } else {
          writer.append(");").eol();
        }
      }
    }
    writer.append("  private AvajeModule aggregatedModule;").eol();
  }

  private void writeBuildMethods() {
    writer.append(
        "\n"
        + "  @Override\n"
        + "  public boolean supportsExpected(List<AvajeModule> modules) {\n"
        + "    if (modules.size() != %s) {\n"
        + "      return false;\n"
        + "    }\n"
        + "    for (AvajeModule module : modules) {\n"
        + "      if (!MODULE_NAMES.contains(module.getClass().getTypeName())) {\n"
        + "        return false;\n"
        + "      }\n"
        + "    }\n"
        + "    return true;\n"
        + "  }\n"
        + "\n"
        + "  @Override\n"
        + "  public List<AvajeModule> factories() {\n"
        + "    return aggregatedModule != null ? List.of(aggregatedModule) : List.of();\n"
        + "  }\n"
        + "\n"
        + "  @Override\n"
        + "  public Set<String> orderModules() {\n"
        + "    return MODULE_NAMES;\n"
        + "  }\n"
        + "\n"
        + "  @Override\n"
        + "  public void add(AvajeModule module) {\n"
        + "    if (!module.strictWiring()) {\n"
        + "      return;\n"
        + "    }\n"
        + "    if (aggregatedModule == null) {\n"
        + "      aggregatedModule = module;\n"
        + "      return;\n"
        + "    }\n"
        + "    throw new IllegalStateException(\"Multiple strict wiring modules are not supported: \"\n"
        + "      + aggregatedModule.getClass().getTypeName() + \", \" + module.getClass().getTypeName());\n"
        + "  }\n"
        + "\n"
        + "  @Override\n"
        + "  public boolean isEmpty() {\n"
        + "    return MODULE_NAMES.isEmpty();\n"
        + "  }\n"
        + "}", numberOfModules);
  }
}
