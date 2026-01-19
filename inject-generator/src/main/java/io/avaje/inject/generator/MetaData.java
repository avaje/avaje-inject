package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the data as per <code>@DependencyMeta</code>
 */
final class MetaData implements Comparable<MetaData> {

  private static final Comparator<MetaData> COMPARATOR =
    Comparator.comparing(MetaData::type)
      .thenComparing(MetaData::name, Comparator.nullsFirst(Comparator.naturalOrder()))
      .thenComparing(MetaData::compareProvides);

  private static final Map<String, Integer> FACTORY_FREQUENCY = new HashMap<>();

  private static final String INDENT = "      ";
  private static final String NEWLINE = "\n";

  private final String type;
  private final String shortType;
  private final String name;
  private final String buildName;
  private final String method;
  private final String key;
  private boolean wired;

  /**
   * The interfaces and class annotations the bean has (to register into lists).
   */
  private List<String> provides;

  /**
   * The list of dependencies with optional and named.
   */
  private List<Dependency> dependsOn;

  private boolean generateProxy;
  private boolean usesExternalDependency;
  private final Set<String> externalDependencies = new HashSet<>();
  private boolean importedComponent;

  MetaData(DependencyMetaPrism meta) {
    this.type = meta.type();
    this.name = trimName(meta.name());
    this.shortType = Util.shortName(type);
    this.method = meta.method();
    this.dependsOn = meta.dependsOn().stream().map(Dependency::new).collect(Collectors.toList());
    this.provides = Util.addQualifierSuffix(meta.provides(), name);
    this.importedComponent = meta.importedComponent();
    this.key = createKey();
    this.buildName = createBuildName();
  }

  MetaData(String type, String name) {
    this(type, name, null);
  }

  MetaData(String type, String name, String method) {
    this.type = ProcessorUtils.trimAnnotations(type);
    this.name = trimName(name);
    this.shortType = Util.shortName(type);
    this.provides = new ArrayList<>();
    this.dependsOn = new ArrayList<>();
    this.method = method;
    this.key = createKey();
    this.buildName = createBuildName();
  }

  @Override
  public String toString() {
    return name == null ? type : type + ':' + name;
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
    return buildName;
  }

  private String createBuildName() {
    if (Util.isVoid(type)) {
      return "void_" + Util.trimMethod(method);
    }
    final String trimType = Util.trimMethod(Util.unwrapProvider(type));
    if (name != null) {
      return trimType + '_' + name.replaceAll("[^a-zA-Z0-9_$]+", "_");
    } else if (buildNameIncludeMethod() || hasMethod() && FACTORY_FREQUENCY.get(type) > 0) {
      return trimType + "__" + Util.trimMethod(method);
    }
    return trimType;
  }

  private boolean buildNameIncludeMethod() {
    // generic type created via factory bean method
    return type.contains("<") && hasMethod();
  }

  String key() {
    return key;
  }

  private String createKey() {
    if (Util.isVoid(type)) {
      return "method:" + method;
    }
    String keyString = name != null ? type + ":" + name : type;
    if (hasMethod() && FACTORY_FREQUENCY.compute(type, (k, v) -> v == null ? 0 : ++v) > 0) {
      // allow multiple factory methods that produce the same types, typically conditional
      keyString += FACTORY_FREQUENCY.get(type);
    }
    return keyString;
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
    this.generateProxy = beanReader.isGenerateProxy();
    this.importedComponent = beanReader.importedComponent();
  }

  String name() {
    return name;
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
        importTypes.add(ProcessingContext.importedPkg(type) + '.' + shortType + Constants.DI);
      } else {
        importTypes.add(type + Constants.DI);
      }
    }
  }

  void buildMethod(Append append, boolean fullyQualify) {
    if (generateProxy) {
      return;
    }
    if (usesExternalDependency) {
      append.append("  // uses external dependency " + externalDependencies + NEWLINE);
    }

    final var hasName = name != null;
    final var hasMethod = hasMethod();
    final var hasDependsOn = !dependsOn.isEmpty();
    final var hasProvides = !provides.isEmpty();

    append.append("  @DependencyMeta(");
    if (hasName || hasMethod || hasDependsOn || hasProvides) {
      append.eol().append(INDENT);
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
    if (hasProvides) {
      appendProvides(append, "provides", provides);
    }
    if (hasDependsOn) {
      appendProvides(append, "dependsOn", dependsOn.stream().map(Dependency::dependsOn).collect(Collectors.toList()));
    }
    append.append(")").append(NEWLINE);
    append.append("  private void build_").append(buildName()).append("(Builder builder) {").append(NEWLINE);
    if (hasMethod()) {
      append.append("    ").append(Util.shortMethod(method)).append("(builder");
    } else {
      append.append("    ").append(fullyQualify ? type : shortType).append(Constants.DI).append(".build(builder");
    }
    append.append(");").append(NEWLINE);
    append.append("  }").append(NEWLINE);
    append.eol();
  }

  private boolean hasMethod() {
    return method != null && !method.isEmpty();
  }

  private void appendProvides(Append sb, String attribute, List<String> types) {
    if (!"dependsOn".equals(attribute)) {
      types.removeIf(s -> s.contains(":"));
    }

    sb.append(",").eol().append(INDENT).append(attribute).append(" = {");
    final var size = types.size();
    if (size > 1) {
      sb.eol().append("        ");
    }
    var seen = new HashSet<String>();
    for (int i = 0; i < types.size(); i++) {
      final var depType = types.get(i);
      if (!seen.add(depType)) {
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
      sb.eol().append(INDENT);
    }
    sb.append("}");
  }

  void setProvides(List<String> provides) {
    this.provides = provides;
  }

  void setDependsOn(List<String> dependsOn) {
    this.dependsOn = dependsOn.stream().map(Dependency::new).collect(Collectors.toList());
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

  private String compareProvides() {
    return provides.toString();
  }

  @Override
  public int compareTo(MetaData meta) {
    return COMPARATOR.compare(this, meta);
  }
}
