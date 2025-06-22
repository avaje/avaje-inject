package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

final class SimpleBeanLazyWriter {
  private static final Set<String> GENERATED_PUBLISHERS = new HashSet<>();
  private static final String TEMPLATE =
      "package {0};\n\n"
          + "{1}"
          + "@Proxy\n"
          + "@Generated(\"avaje-inject-generator\")\n"
          + "public final class {2}$Lazy{3} {4} {2}{3} '{'\n"
          + "\n"
          + "  private final Provider<{2}{3}> onceProvider;\n"
          + "\n"
          + "  public {2}$Lazy(Provider<{2}{3}> onceProvider) '{'\n"
          + "    this.onceProvider = onceProvider;\n"
          + "  '}'\n\n"
          + "{5}"
          + "'}'\n";

  private final String originName;
  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final String packageName;

  static void write(PackageElement pkg, TypeElement element) {
    new SimpleBeanLazyWriter(pkg, element);
  }

  private final String shortName;
  private final boolean isInterface;
  private final TypeElement element;

  SimpleBeanLazyWriter(PackageElement pkg, TypeElement element) {
    this.element = element;
    this.isInterface = element.getKind().isInterface();
    this.shortName = Util.shortName(element.getQualifiedName().toString()).replace(".", "_");
    this.packageName = pkg.getQualifiedName().toString();
    this.originName = packageName + "." + shortName + "$Lazy";

    if (GENERATED_PUBLISHERS.contains(originName)) {
      return;
    }

    importTypes.addAll(UType.parse(element.asType()).importTypes());
    write();
    GENERATED_PUBLISHERS.add(originName);
  }

  void write() {
    try {
      final var writer = new Append(APContext.createSourceFile(originName, element).openWriter());

      var inheritance = isInterface ? "implements" : "extends";
      String methodString = methods();

      // Get type parameters
      List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
      String typeParametersDecl = buildTypeParametersDeclaration(typeParameters);
      writer.append(
        MessageFormat.format(
          TEMPLATE,
          packageName,
          imports(),
          shortName,
          typeParametersDecl,
          inheritance,
          methodString));
      writer.close();
    } catch (Exception e) {
      logError("Failed to write Proxy class %s", e);
    }
  }

  String imports() {
    importTypes.add("io.avaje.inject.spi.Proxy");
    importTypes.add("io.avaje.inject.spi.Generated");
    importTypes.add("jakarta.inject.Provider");

    StringBuilder writer = new StringBuilder();
    for (String importType : importTypes.forImport()) {
      if (Util.validImportType(importType, packageName)) {
        writer.append(String.format("import %s;\n", Util.sanitizeImports(importType)));
      }
    }
    return writer.append("\n").toString();
  }

  private String methods() {
    var sb = new StringBuilder();
    for (var methodElement : ElementFilter.methodsIn(APContext.elements().getAllMembers(element))) {

      Set<Modifier> modifiers = methodElement.getModifiers();
      if (modifiers.contains(Modifier.PRIVATE)
          || modifiers.contains(Modifier.STATIC)
          || methodElement.getEnclosingElement().getSimpleName().contentEquals("Object")) continue;
      // Access modifiers
      sb.append("  @Override\n");
      if (modifiers.contains(Modifier.PUBLIC)) {
        sb.append("  public ");
      } else if (modifiers.contains(Modifier.PROTECTED)) {
        sb.append("  protected ");
      } else {
        sb.append("  ");
      }
      // Generic type parameters
      List<? extends TypeParameterElement> typeParameters = methodElement.getTypeParameters();
      if (!typeParameters.isEmpty()) {
        sb.append("<");
        sb.append(
          typeParameters.stream()
            .map(tp -> tp.getSimpleName().toString())
            .collect(Collectors.joining(", ")));
        sb.append("> ");
      }
      var returnType = UType.parse(methodElement.getReturnType());
      importTypes.addAll(returnType.importTypes());
      sb.append(returnType.shortType()).append(" ");

      // Method name
      String methodName = methodElement.getSimpleName().toString();
      sb.append(methodName);

      // Parameters
      sb.append("(");
      var parameters = methodElement.getParameters();
      for (int i = 0; i < parameters.size(); i++) {
        VariableElement param = parameters.get(i);

        var type = UType.parse(param.asType());
        importTypes.addAll(type.importTypes());
        sb.append(type.shortType());
        sb.append(" ");
        sb.append(param.getSimpleName().toString());
        if (i < parameters.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append(")");

      // Thrown exceptions
      var thrownTypes = methodElement.getThrownTypes();
      if (!thrownTypes.isEmpty()) {
        sb.append(" throws ");
        sb.append(
          thrownTypes.stream()
            .map(t -> UType.parse(t).shortType())
            .collect(Collectors.joining(", ")));
      }

      sb.append(" {\n    ");
      if (!"void".equals(returnType.full())) {
        sb.append("return ");
      }

      sb.append("onceProvider.get().").append(methodName);
      sb.append("(");
      for (int i = 0; i < parameters.size(); i++) {
        sb.append(parameters.get(i).getSimpleName().toString());
        if (i < parameters.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append(");\n");
      sb.append("  }\n\n");
    }
    return sb.toString();
  }

  private String buildTypeParametersDeclaration(List<? extends TypeParameterElement> typeParameters) {
    if (typeParameters.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder("<");
    for (int i = 0; i < typeParameters.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      TypeParameterElement param = typeParameters.get(i);
      sb.append(param.getSimpleName());

      List<? extends TypeMirror> bounds = param.getBounds();
      if (!bounds.isEmpty() && !"java.lang.Object".equals(bounds.get(0).toString())) {
        sb.append(" extends ");
        for (int j = 0; j < bounds.size(); j++) {
          if (j > 0) {
            sb.append(" & ");
          }
          sb.append(bounds.get(j).toString());
        }
      }
    }
    sb.append(">");
    return sb.toString();
  }
}
