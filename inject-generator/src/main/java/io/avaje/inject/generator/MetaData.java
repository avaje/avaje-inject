package io.avaje.inject.generator;

import io.avaje.inject.spi.DependencyMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

  /**
   * The interfaces and class annotations the bean has (to register into lists).
   */
  private List<String> provides;

  /**
   * The list of dependencies with optional and named.
   */
  private List<Dependency> dependsOn;
  private boolean generateProxy;

  MetaData(DependencyMeta meta) {
    this.type = meta.type();
    this.name = trimName(meta.name());
    this.shortType = Util.shortName(type);
    this.method = meta.method();
    this.provides = asList(meta.provides());
    this.dependsOn = Stream.of(meta.dependsOn()).map(Dependency::new).collect(Collectors.toList());
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

  /**
   * Return true if this is a component with Aspects applied to it.
   * This means this type doesn't have a $DI but instead we have the $Proxy$DI.
   */
  boolean isGenerateProxy() {
    return generateProxy;
  }

  private String trimName(String name) {
    return "".equals(name) ? null : name;
  }

  String getBuildName() {
    if (Util.isVoid(type)) {
      return "void_" + Util.trimMethod(method);
    } else {
      String trimType = Util.trimMethod(type);
      if (name != null) {
        return trimType + "_" + name;
      } else {
        return trimType;
      }
    }
  }

  public String getKey() {
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

  private List<String> asList(String[] content) {
    if (content == null || content.length == 0) {
      return new ArrayList<>();
    }
    return Arrays.asList(content);
  }

  void update(BeanReader beanReader) {
    this.provides = beanReader.getProvides();
    this.dependsOn = beanReader.getDependsOn();
    this.generateProxy = beanReader.isGenerateProxy();
  }

  String getType() {
    return type;
  }

  List<String> getProvides() {
    return provides;
  }

  List<Dependency> getDependsOn() {
    return dependsOn;
  }

  /**
   * Return the top level package for the bean and the interfaces it implements.
   */
  String getTopPackage() {
    if (method == null || method.isEmpty()) {
      return Util.packageOf(type);
    }
    // ignore Beans from @Bean factory methods
    return null;
  }

  void addImportTypes(Set<String> importTypes) {
    if (hasMethod()) {
      importTypes.add(Util.classOfMethod(method));
    } else if (!generateProxy) {
      importTypes.add(type + Constants.DI);
    }
  }

  void buildMethod(Append append) {
    if (generateProxy) {
      return;
    }
    append.append("  @DependencyMeta(type=\"").append(type).append("\"");
    if (name != null) {
      append.append(", name=\"").append(name).append("\"");
    }
    if (hasMethod()) {
      append.append(", method=\"").append(method).append("\"");
    }
    if (!provides.isEmpty()) {
      appendProvides(append, "provides", provides);
    }
    if (!dependsOn.isEmpty()) {
      appendProvides(append, "dependsOn", dependsOn.stream().map(Dependency::dependsOn).collect(Collectors.toList()));
    }
    append.append(")").append(NEWLINE);
    append.append("  private void build_").append(getBuildName()).append("() {").append(NEWLINE);
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
    sb.append(", ").append(attribute).append("={");
    for (int i = 0; i < types.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("\"");
      sb.append(types.get(i));
      sb.append("\"");
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

}
