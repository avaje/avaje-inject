package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static io.avaje.inject.generator.APContext.logError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/** Write the source code for the bean. */
final class AutoFactoryWriter {

  private static final String SUFFIX = "$AutoFactory";

  private static final Set<String> COPY_ANNOTATIONS =
      Set.of(
          PrimaryPrism.PRISM_TYPE,
          SecondaryPrism.PRISM_TYPE,
          PrototypePrism.PRISM_TYPE,
          LazyPrism.PRISM_TYPE,
          NamedPrism.PRISM_TYPE);

  private final TypeElement type;
  private final String creatorType;
  private final String packageName;
  private final String factoryShortName;
  private final Element origin;

  private AutoFactoryWriter(
      TypeElement type, String creatorType, String packageName, Element origin) {
    this.type = type;
    this.creatorType = creatorType;
    this.packageName = packageName;
    this.origin = origin;
    final String typePackage = packageOf(type);
    final String qualifiedName = type.getQualifiedName().toString();
    final String nestedName =
        typePackage.isEmpty() ? qualifiedName : qualifiedName.substring(typePackage.length() + 1);
    this.factoryShortName = nestedName.replace('.', '$') + SUFFIX;
  }

  static void write(TypeElement type) {
    if (!isValid(type, type, false)) {
      return;
    }
    final var creator = APContext.asTypeElement(AutoFactoryPrism.getInstanceOn(type).value());
    new AutoFactoryWriter(type, creator.getQualifiedName().toString(), packageOf(type), type)
        .tryWrite();
  }

  static void writeImport(TypeElement type, TypeElement creator, Element base) {
    if (!isValid(type, base, true)) {
      return;
    }
    new AutoFactoryWriter(type, creator.getQualifiedName().toString(), importPackage(base), base)
        .tryWrite();
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
      logError(origin, "Failed to write @AutoFactory factory %s", e.getMessage());
    }
  }

  private void write() throws IOException {
    final List<String> annotations = new ArrayList<>();
    final var imports = new ImportTypeMap();
    imports.add("io.avaje.inject.Bean");
    imports.add("io.avaje.inject.Factory");
    imports.add("io.avaje.inject.spi.Generated");
    readAnnotations(annotations, imports);
    final String beanType = type.getQualifiedName().toString();
    final String beanShortType = imports.add(beanType);
    final String creatorShortType = imports.add(creatorType);

    final String fullName =
        packageName.isEmpty() ? factoryShortName : packageName + "." + factoryShortName;
    final var writer = new Append(createSourceFile(fullName, origin).openWriter());
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
    writer.append(" * Generated source - Factory for @AutoFactory %s.", beanType).eol();
    writer.append(" */").eol();
    writer.append(Constants.AT_SUPPRESS_WARNINGS).eol();
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@Factory").eol();
    writer.append("final %sclass %s {", Util.valhalla(), factoryShortName).eol().eol();

    writer.append("  /**").eol();
    writer.append("   * Create %s using the %s creator.", beanType, creatorType).eol();
    writer.append("   */").eol();
    writer.append("  @Bean").eol();
    for (final String annotation : annotations) {
      writer.append("  %s", annotation).eol();
    }
    writer.append("  %s build(%s creator) {", beanShortType, creatorShortType).eol();
    writer.append("    return creator.create(%s.class);", beanShortType).eol();
    writer.append("  }").eol();
    writer.append("}").eol();
    writer.close();
  }

  private boolean samePackage(String importType) {
    final int dot = importType.lastIndexOf('.');
    return dot == -1 ? packageName.isEmpty() : importType.substring(0, dot).equals(packageName);
  }

  private void readAnnotations(List<String> annotations, ImportTypeMap imports) {
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
  }
}
