package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

/** Write the source code for the bean. */
final class EventPublisherWriter {

  private static final String TEMPLATE =
      "package {0};\n\n"
          + "{1}"
          + "@Component\n"
          + "@Generated(\"avaje-inject-generator\")\n"
          + "public class {2}Publisher extends Event<{2}> '{'\n"
          + "\n"
          + "  public {2}Publisher(ObserverManager manager) '{'\n"
          + "    super(manager.observersByType({2}.class));\n"
          + "  '}'\n"
          + "'}'\n";
  private final String originName;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final UType utype;
  private final String packageName;

  EventPublisherWriter(Element element) {
    this.packageName = APContext.elements().getPackageOf(element).getQualifiedName().toString();

    this.utype = UType.parse(element.asType());
    this.originName = utype.mainType() + "Publisher";
    importTypes.addAll(utype.importTypes());
    if (utype.isGeneric()) {
      APContext.logError(
          element,
          "Event publishers generation may not be used for generic classes. Generic event publishers must be constructed manually");
    }
    write();
  }

  private Writer createFileWriter() throws IOException {
    final JavaFileObject jfo = createSourceFile(originName);
    return jfo.openWriter();
  }

  void write() {
    try {
      var writer = new Append(createFileWriter());
      writer.append(MessageFormat.format(TEMPLATE, packageName, imports(), utype.shortType()));
      writer.close();
    } catch (Exception e) {
    }
  }

  String imports() {
    importTypes.add("io.avaje.inject.Component");
    importTypes.add("io.avaje.inject.events.Event");
    importTypes.add("io.avaje.inject.events.ObserverManager");
    importTypes.add("io.avaje.inject.spi.Generated");
    StringBuilder writer = new StringBuilder();
    for (String importType : importTypes.forImport()) {
      if (Util.validImportType(importType)) {
        writer.append(String.format("import %s;\n", Util.sanitizeImports(importType)));
      }
    }
    return writer.append("\n").toString();
  }
}
