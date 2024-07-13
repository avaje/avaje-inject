package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.typeElement;
import static java.util.stream.Collectors.toList;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Holds the data as per <code>@DependencyMeta</code>
 */
final class MetaData {

  private static final String NEWLINE = "\n";

  private final String type;
  private final String shortType;
  private final String name;
  private String method;
  private boolean wired;
  private String providesAspect;

  /**
   * The interfaces and class annotations the bean has (to register into lists).
   */
  private List<String> provides;

  /**
   * The list of dependencies with optional and named.
   */
  private List<Dependency> dependsOn;

  /**
   * Type deemed to be candidate for providing to another external module.
   */
  private List<String> autoProvides;

  private boolean generateProxy;
  private boolean usesExternalDependency;
  private final Set<String> externalDependencies = new HashSet<>();
  private boolean importedComponent;

  MetaData(DependencyMetaPrism meta) {
    this.type = meta.type();
    this.name = trimName(meta.name());
    this.shortType = Util.shortName(type);
    this.method = meta.method();
    this.providesAspect = meta.providesAspect();
    this.provides =
        Stream.concat(
                meta.provides().stream().map(s -> Util.addQualifierSuffix(name, s)),
                meta.provides().stream())
            .collect(toList());
    this.dependsOn = meta.dependsOn().stream().map(Dependency::new).collect(Collectors.toList());
    this.autoProvides =
        Stream.concat(
                meta.autoProvides().stream().map(s -> Util.addQualifierSuffix(name, s)),
                meta.autoProvides().stream())
            .collect(toList());
    this.importedComponent = meta.importedComponent();
  }

  MetaData(String type, String name) {
    this.type = type;
    this.name = trimName(name);
    this.shortType = Util.shortName(type);
    this.provides = new ArrayList<>();
    this.dependsOn = new ArrayList<>();
  }

  @Override
  public String toString() {
    return (name == null) ? type : type + ":" + name;
  }

  boolean importedComponent() {
    return importedComponent;
  }

  /**
   * Return true if this is a component with Aspects applied to it.
   * This means this type doesn't have a $DI but instead we have the $Proxy$DI.
   */
  boolean isGenerateProxy() {
    return generateProxy;
  }

  private String trimName(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    return name.replace("\\\"", "'");
  }

  String buildName() {
    if (Util.isVoid(type)) {
      return "void_" + Util.trimMethod(method);
    } else {
      final String trimType = Util.trimMethod(Util.unwrapProvider(type));
      if (name != null) {
        return trimType + "_" + name.replaceAll("[^a-zA-Z0-9_$]+", "_");
      } else {
        if (buildNameIncludeMethod()) {
          return trimType + "__" + Util.trimMethod(method);
        }
        return trimType;
      }
    }
  }

  private boolean buildNameIncludeMethod() {
    // generic type created via factory bean method
    return type.contains("<") && hasMethod();
  }

  public String key() {
    if (Util.isVoid(type)) {
      return "method:" + method;
    }
    if (name != null) {
      return type + ":" + name;
    } else {
      return type;
    }
  }

  boolean noDepends() {
    return dependsOn == null || dependsOn.isEmpty();
  }

  boolean isWired() {
    return wired;
  }

  void setWired() {
    this.wired = true;
  }

  void update(BeanReader beanReader) {
    this.provides = beanReader.provides();
    this.dependsOn = beanReader.dependsOn();
    this.providesAspect = beanReader.providesAspect();
    this.autoProvides = beanReader.autoProvides();
    this.generateProxy = beanReader.isGenerateProxy();
    this.importedComponent = beanReader.importedComponent();
  }

  String type() {
    return type;
  }

  List<String> provides() {
    return provides;
  }

  List<Dependency> dependsOn() {
    return dependsOn;
  }

  List<String> autoProvides() {
    return autoProvides;
  }

  String providesAspect() {
    return providesAspect;
  }

  /**
   * Return the top level package for the bean and the interfaces it implements.
   */
  String topPackage() {
    if (method == null || method.isEmpty()) {
      return ProcessorUtils.packageOf(type);
    }
    // ignore Beans from @Bean factory methods
    return null;
  }

  void addImportTypes(Set<String> importTypes) {
    if (hasMethod()) {
      importTypes.add(Util.classOfMethod(method));
    } else if (!generateProxy) {
      if (importedComponent) {
        String packageName;
        if (typeElement(type).getNestingKind().isNested()) {
          packageName = Util.nestedPackageOf(type);
        } else {
          packageName = ProcessorUtils.packageOf(type);
        }
        importTypes.add(packageName + ".di." + shortType + Constants.DI);
      } else {
        importTypes.add(type + Constants.DI);
      }
    }
  }

  void buildMethod(Append append) {
    if (generateProxy) {
      return;
    }
    if (usesExternalDependency) {
      append.append("  // uses external dependency " + externalDependencies + NEWLINE);
    }

    final var hasName = name != null;
    final var hasMethod = hasMethod();
    final var hasProvidesAspect = !providesAspect.isEmpty();
    final var hasDependsOn = !dependsOn.isEmpty();
    final var hasProvides = !provides.isEmpty();
    final var hasAutoProvides = autoProvides != null && !autoProvides.isEmpty();

    append.append("  @DependencyMeta(");
    if (hasName
        || hasMethod
        || hasProvidesAspect
        || hasDependsOn
        || hasProvides
        || hasAutoProvides) {
      append.eol().append("      ");
    }

    append.append("type = \"").append(type).append("\"");
    if (hasName) {
      append.append(",").eol().append("      name = \"").append(name).append("\"");
    }
    if (importedComponent) {
      append.append(",").eol().append("      importedComponent = true");
    }
    if (hasMethod) {
      append.append(",").eol().append("      method = \"").append(method).append("\"");
    }
    if (hasProvidesAspect) {
      append.append(",").eol().append("      providesAspect = \"").append(providesAspect).append("\"");
    } else if (hasProvides) {
      appendProvides(append, "provides", provides);
    }
    if (hasDependsOn) {
      appendProvides(append, "dependsOn", dependsOn.stream().map(Dependency::dependsOn).collect(Collectors.toList()));
    }
    if (hasAutoProvides) {
      appendProvides(append, "autoProvides", autoProvides);
    }
    append.append(")").append(NEWLINE);
    append.append("  private void build_").append(buildName()).append("() {").append(NEWLINE);
    if (hasMethod()) {
      append.append("    ").append(Util.shortMethod(method)).append("(builder");
    } else {
      append.append("    ").append(shortType).append(Constants.DI).append(".build(builder");
    }
    append.append(");").append(NEWLINE);
    append.append("  }").append(NEWLINE);
    append.eol();
  }

  private boolean hasMethod() {
    return method != null && !method.isEmpty();
  }

  private void appendProvides(Append sb, String attribute, List<String> types) {

    sb.append(",").eol().append("      ").append(attribute).append(" = {");
    final var size = types.size();
    if (size > 1) {
      sb.eol().append("        ");
    }
    var seen = new HashSet<String>();
    for (int i = 0; i < types.size(); i++) {

      final var depType = types.get(i);
      if (!"dependsOn".equals(attribute) && depType.contains(":") || !seen.add(depType)) {
        continue;
      }
      if (i > 0) {
        sb.append(",").eol().append("        ");
      }
      sb.append("\"");
      sb.append(depType);
      sb.append("\"");
    }
    if (size > 1) {
      sb.eol().append("      ");
    }
    sb.append("}");
  }

  void setProvides(List<String> provides) {
    this.provides = provides;
  }

  void setDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn.stream().map(Dependency::new).collect(Collectors.toList());
  }

  void setMethod(String method) {
    this.method = method;
  }

  void setAutoProvides(List<String> autoProvides) {
    this.autoProvides = autoProvides;
  }

  void setProvidesAspect(String providesAspect) {
    this.providesAspect = providesAspect;
  }

  /**
   * This depends on a dependency that comes from another module in the classpath.
   */
  void markWithExternalDependency(String name) {
    usesExternalDependency = true;
    externalDependencies.add(name);
    for (Dependency dependency : dependsOn) {
      if (name.equals(dependency.name())) {
        dependency.markExternal();
      }
    }
  }
}
