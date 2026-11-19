package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static io.avaje.inject.generator.APContext.logError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/** Write the source code for the bean. */
final class AutoFactoryWriter {

  private static final String SUFFIX = "$AutoFactory";
  private static final String IMPORT_SUFFIX = "$AutoFactoryImports";

  private static final Set<String> COPY_ANNOTATIONS =
      Set.of(
          PrimaryPrism.PRISM_TYPE,
          SecondaryPrism.PRISM_TYPE,
          PrototypePrism.PRISM_TYPE,
          LazyPrism.PRISM_TYPE,
          NamedPrism.PRISM_TYPE);

  private final List<Entry> entries;
  private final String packageName;
  private final String factoryShortName;
  private final List<Element> origins;
  private final String comment;

  private static final class Entry {
    final TypeElement type;
    final String creatorType;
    final String methodName;

    Entry(TypeElement type, TypeElement creator, String methodName) {
      this.type = type;
      this.creatorType = creator.getQualifiedName().toString();
      this.methodName = methodName;
    }
  }

  private AutoFactoryWriter(
      List<Entry> entries,
      String packageName,
      String factoryShortName,
      List<Element> origins,
      String comment) {
    this.entries = entries;
    this.packageName = packageName;
    this.factoryShortName = factoryShortName;
    this.origins = origins;
    this.comment = comment;
  }

  static void write(TypeElement type) {
    if (!isValid(type, type, false)) {
      return;
    }
    final var creator = APContext.asTypeElement(AutoFactoryPrism.getInstanceOn(type).value());
    final var entry = new Entry(type, creator, "build");
    new AutoFactoryWriter(
            List.of(entry),
            packageOf(type),
            nestedName(type).replace('.', '$') + SUFFIX,
            List.of(type),
            "@AutoFactory " + type.getQualifiedName())
        .tryWrite();
  }

  static void writeImports(Element base, Map<TypeElement, TypeElement> typeCreators) {
    final List<Entry> entries = new ArrayList<>();
    final Set<String> methodNames = new HashSet<>();
    typeCreators.forEach(
        (type, creator) -> {
          if (isValid(type, base, true)) {
            entries.add(new Entry(type, creator, uniqueMethodName(type, methodNames)));
          }
        });
    if (entries.isEmpty()) {
      return;
    }
    new AutoFactoryWriter(
            entries,
            importPackage(base),
            importFactoryName(base),
            List.of(base),
            "@AutoFactory.Import on " + base)
        .tryWrite();
  }

  static void writePackage(PackageElement pkg) {
    final var creator = APContext.asTypeElement(AutoFactoryPrism.getInstanceOn(pkg).value());
    final List<TypeElement> types = new ArrayList<>();
    ElementFilter.typesIn(pkg.getEnclosedElements())
        .forEach(t -> collectPackageTypes(t, creator, types));

    final List<Entry> entries = new ArrayList<>();
    final Set<String> methodNames = new HashSet<>();
    for (final TypeElement type : types) {
      entries.add(new Entry(type, creator, uniqueMethodName(type, methodNames)));
    }
    if (entries.isEmpty()) {
      APContext.logWarn(
          pkg,
          "@AutoFactory package %s has no interfaces or abstract classes",
          pkg.getQualifiedName());
      return;
    }
    final List<Element> origins = new ArrayList<>();
    origins.add(pkg);
    origins.addAll(types);
    new AutoFactoryWriter(
            entries,
            pkg.getQualifiedName().toString(),
            "package" + SUFFIX,
            origins,
            "@AutoFactory package " + pkg.getQualifiedName())
        .tryWrite();
  }

  private static void collectPackageTypes(
      TypeElement type, TypeElement creator, List<TypeElement> types) {
    if (type.getModifiers().contains(Modifier.PRIVATE)) {
      return;
    }
    if (isPackageCandidate(type, creator)) {
      if (type.getTypeParameters().isEmpty()) {
        types.add(type);
      } else {
        APContext.logWarn(
            type,
            "@AutoFactory package skipping %s as it has type parameters",
            type.getQualifiedName());
      }
    }
    ElementFilter.typesIn(type.getEnclosedElements())
        .forEach(t -> collectPackageTypes(t, creator, types));
  }

  private static boolean isPackageCandidate(TypeElement type, TypeElement creator) {
    final boolean isInterface = type.getKind() == ElementKind.INTERFACE;
    final boolean isAbstractClass =
        type.getKind() == ElementKind.CLASS && type.getModifiers().contains(Modifier.ABSTRACT);
    return (isInterface || isAbstractClass)
        && !AutoFactoryPrism.isPresent(type)
        && !type.equals(creator);
  }

  private static String uniqueMethodName(TypeElement type, Set<String> methodNames) {
    final String name = "build" + nestedName(type).replace(".", "");
    String unique = name;
    for (int i = 1; !methodNames.add(unique); i++) {
      unique = name + i;
    }
    return unique;
  }

  private static String importFactoryName(Element base) {
    if (base instanceof ModuleElement) {
      return "module" + IMPORT_SUFFIX;
    }
    if (base instanceof PackageElement) {
      return "package" + IMPORT_SUFFIX;
    }
    return nestedName((TypeElement) base).replace('.', '$') + IMPORT_SUFFIX;
  }

  private static String nestedName(TypeElement type) {
    final String typePackage = packageOf(type);
    final String qualifiedName = type.getQualifiedName().toString();
    return typePackage.isEmpty()
        ? qualifiedName
        : qualifiedName.substring(typePackage.length() + 1);
  }

  private static String packageOf(Element element) {
    return APContext.elements().getPackageOf(element).getQualifiedName().toString();
  }

  private static String importPackage(Element base) {
    if (base instanceof ModuleElement) {
      return base.getEnclosedElements().get(0).getSimpleName().toString();
    }
    if (base instanceof PackageElement) {
      return ((PackageElement) base).getQualifiedName().toString();
    }
    return packageOf(base);
  }

  private static boolean isValid(TypeElement type, Element errorElement, boolean imported) {
    final String prefix =
        imported ? "@AutoFactory.Import type " + type.getQualifiedName() : "@AutoFactory";
    final boolean isInterface = type.getKind() == ElementKind.INTERFACE;
    final boolean isAbstractClass =
        type.getKind() == ElementKind.CLASS && type.getModifiers().contains(Modifier.ABSTRACT);
    if (!isInterface && !isAbstractClass) {
      logError(errorElement, "%s must be an interface or abstract class", prefix);
      return false;
    }
    if (!type.getTypeParameters().isEmpty()) {
      logError(errorElement, "%s cannot have type parameters", prefix);
      return false;
    }
    if (imported && !type.getModifiers().contains(Modifier.PUBLIC)) {
      logError(errorElement, "%s must be public", prefix);
      return false;
    }
    return true;
  }

  private void tryWrite() {
    try {
      write();
    } catch (IOException e) {
      logError(origins.get(0), "Failed to write @AutoFactory factory %s", e.getMessage());
    }
  }

  private void write() throws IOException {
    final var imports = new ImportTypeMap();
    imports.add("io.avaje.inject.Bean");
    imports.add("io.avaje.inject.Factory");
    imports.add("io.avaje.inject.spi.Generated");
    final List<String> methods = new ArrayList<>();
    for (final Entry entry : entries) {
      methods.add(method(entry, imports));
    }

    final String fullName =
        packageName.isEmpty() ? factoryShortName : packageName + "." + factoryShortName;
    final var writer =
        new Append(createSourceFile(fullName, origins.toArray(new Element[0])).openWriter());
    if (!packageName.isEmpty()) {
      writer.append("package %s;", packageName).eol().eol();
    }
    for (final String importType : imports.forImport()) {
      if (!samePackage(importType)) {
        writer.append("import %s;", importType).eol();
      }
    }
    writer.eol();

    writer.append("/**").eol();
    writer.append(" * Generated source - Factory for %s.", comment).eol();
    writer.append(" */").eol();
    writer.append("@Factory").eol();
    writer.append(Constants.AT_SUPPRESS_WARNINGS).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("final %sclass %s {", Util.valhalla(), factoryShortName).eol();
    for (final String method : methods) {
      writer.eol().append(method);
    }
    writer.append("}").eol();
    writer.close();
  }

  private String method(Entry entry, ImportTypeMap imports) {
    final List<String> annotations = readAnnotations(entry.type, imports);
    final String beanType = entry.type.getQualifiedName().toString();
    final String beanShortType = imports.add(beanType);
    final String creatorShortType = imports.add(entry.creatorType);

    final var sb = new StringBuilder();
    sb.append("  /**\n");
    sb.append("   * Create ")
        .append(beanType)
        .append(" using the ")
        .append(entry.creatorType)
        .append(" creator.\n");
    sb.append("   */\n");
    sb.append("  @Bean\n");
    for (final String annotation : annotations) {
      sb.append("  ").append(annotation).append("\n");
    }
    sb.append("  ")
        .append(beanShortType)
        .append(" ")
        .append(entry.methodName)
        .append("(")
        .append(creatorShortType)
        .append(" creator) {\n");
    sb.append("    return creator.create(").append(beanShortType).append(".class);\n");
    sb.append("  }\n");
    return sb.toString();
  }

  private boolean samePackage(String importType) {
    final int dot = importType.lastIndexOf('.');
    return dot == -1 ? packageName.isEmpty() : importType.substring(0, dot).equals(packageName);
  }

  private static List<String> readAnnotations(TypeElement type, ImportTypeMap imports) {
    final List<String> annotations = new ArrayList<>();
    for (final AnnotationMirror mirror : type.getAnnotationMirrors()) {
      final var annotationType = (TypeElement) mirror.getAnnotationType().asElement();
      final String name = annotationType.getQualifiedName().toString();
      final String annotation = AnnotationCopier.toAnnotationString("", mirror, false);
      if (COPY_ANNOTATIONS.contains(name)) {
        annotations.add("@" + imports.add(name) + annotation.substring(name.length() + 1));
      } else if (QualifierPrism.isPresent(annotationType)) {
        annotations.add(annotation);
      }
    }
    return annotations;
  }
}
