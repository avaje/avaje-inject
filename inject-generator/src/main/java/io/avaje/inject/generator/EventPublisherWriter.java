package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static io.avaje.inject.generator.APContext.logError;
import static java.util.function.Predicate.not;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;

/** Write the source code for the bean. */
final class EventPublisherWriter {
  private static final Map<String, String> GENERATED_PUBLISHERS = new HashMap<>();
  private static final String TEMPLATE =
      "package {0};\n\n"
          + "{1}"
          + "@Component\n"
          + "{2}"
          + "@Generated(\"avaje-inject-generator\")\n"
          + "public class {3} extends Event<{4}> '{'\n"
          + "\n"
          + "  private static final Type TYPE = {5};\n"
          + "\n"
          + "  public {3}(ObserverManager manager) '{'\n"
          + "    super(manager, TYPE, \"{6}\");\n"
          + "  '}'\n"
          + "'}'\n";
  private final String originName;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final UType utype;
  private final String packageName;
  private final String qualifier;

  static void write(Element element) {
    new EventPublisherWriter(element);
  }

  private EventPublisherWriter(Element element) {
    final var asType = element.asType();
    this.utype = UType.parse(asType).param0();
    this.packageName = Optional.ofNullable(APContext.typeElement(utype.mainType()))
      .map(APContext.elements()::getPackageOf)
      .map(PackageElement::getQualifiedName)
      .map(Object::toString)
      .orElse("error.notype")
      .replaceFirst("java.", "")
      + ".events";

    this.qualifier = Optional.ofNullable(Util.named(element)).orElse("");
    var className =
      packageName
        + "."
        + (qualifier.isEmpty() ? "" : "Qualified")
        + Util.shortName(utype).replace(".", "_")
        + "_Publisher";

    this.originName = getUniqueClassName(className, 0);
    if (GENERATED_PUBLISHERS.containsKey(originName)) {
      //in super niche situations when compiling the same module, we need to tell avaje that these types already exist
      //got this when running both my eclipse compiler and then the terminal build
      ProcessingContext.addOptionalType(UType.parse(asType).fullWithoutAnnotations(), Util.named(element));
      return;
    }
    importTypes.addAll(utype.importTypes());
    write();
    GENERATED_PUBLISHERS.put(originName, qualifier);
  }

  private String getUniqueClassName(String className, Integer recursiveIndex) {
    Optional.ofNullable(APContext.typeElement(className)).ifPresent(e ->
      GENERATED_PUBLISHERS.put(
        e.getQualifiedName().toString(),
        Optional.ofNullable(Util.named(e)).orElse("")));

    if (Optional.ofNullable(GENERATED_PUBLISHERS.get(className))
        .filter(not(qualifier::equals))
        .isPresent()) {
      var index = className.indexOf("_Publisher");
      className = className.substring(0, index) + "_Publisher" + ++recursiveIndex;
      return getUniqueClassName(className, recursiveIndex);
    }
    return className;
  }

  private Writer createFileWriter() throws IOException {
    final JavaFileObject jfo = createSourceFile(originName);
    return jfo.openWriter();
  }

  void write() {
    try {
      var writer = new Append(createFileWriter());
      final var shortType = utype.shortWithoutAnnotations();
      var typeString = utype.isGeneric() ? getGenericType() : shortType + ".class";

      var name = qualifier.isBlank() ? "" : "@Named(\"" + qualifier + "\")\n";
      var className = originName.replace(packageName + ".", "");
      writer.append(MessageFormat.format(TEMPLATE, packageName, imports(), name, className, shortType, typeString, qualifier));
      writer.close();
    } catch (Exception e) {
      logError("Failed to write EventPublisher class %s", e);
    }
  }

  String imports() {
    importTypes.add("io.avaje.inject.Component");
    importTypes.add("io.avaje.inject.events.Event");
    importTypes.add("io.avaje.inject.events.ObserverManager");
    importTypes.add("io.avaje.inject.spi.Generated");
    importTypes.add(Type.class.getCanonicalName());
    if (!qualifier.isBlank()) {
      importTypes.add(NamedPrism.PRISM_TYPE);
    }
    if (utype.isGeneric()) {
      importTypes.add("io.avaje.inject.spi.GenericType");
    }

    StringBuilder writer = new StringBuilder();
    for (String importType : importTypes.forImport()) {
      if (Util.validImportType(importType, packageName)) {
        writer.append(String.format("import %s;\n", Util.sanitizeImports(importType)));
      }
    }
    return writer.append("\n").toString();
  }

  String getGenericType() {
    var sb = new StringBuilder();
    sb.append("\n      new GenericType<");
    writeGenericType(utype, new HashMap<>(), sb);
    sb.append(">(){}.type();");
    return sb.toString();
  }

  private void writeGenericType(UType type, Map<String, String> seenShortNames, StringBuilder writer) {
    final var typeShortName = Util.shortName(type.mainType());
    final var mainType = seenShortNames.computeIfAbsent(typeShortName, k -> type.mainType());
    if (type.isGeneric()) {
      final var shortName = Objects.equals(type.mainType(), mainType) ? typeShortName : type.mainType();
      writer.append(shortName);
      writer.append("<");
      boolean first = true;
      for (final var param : type.componentTypes()) {
        if (first) {
          first = false;
          writeGenericType(param, seenShortNames, writer);
          continue;
        }
        writer.append(", ");
        writeGenericType(param, seenShortNames, writer);
      }
      writer.append(">");
    } else {
      final var shortName = Objects.equals(type.mainType(), mainType) ? typeShortName : type.mainType();
      writer.append(shortName);
    }
  }
}
