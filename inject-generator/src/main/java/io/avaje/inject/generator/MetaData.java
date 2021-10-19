package io.avaje.inject.generator;

import io.avaje.inject.spi.DependencyMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Holds the data as per <code>@DependencyMeta</code>
 */
class MetaData {

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
  private List<String> dependsOn;

  MetaData(DependencyMeta meta) {
    this.type = meta.type();
    this.name = trimName(meta.name());
    this.shortType = Util.shortName(type);
    this.method = meta.method();
    this.provides = asList(meta.provides());
    this.dependsOn = asList(meta.dependsOn());
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
    this.provides = beanReader.getInterfaces();
    this.dependsOn = beanReader.getDependsOn();
  }

  String getType() {
    return type;
  }

  List<String> getProvides() {
    return provides;
  }

  List<String> getDependsOn() {
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

    } else {
      importTypes.add(type + Constants.DI);
    }
    if (provides != null) {
      for (String provide : provides) {
        if (GenericType.isGeneric(provide)) {
          // provide implementation of generic interface
          importTypes.add(type);
        }
      }
    }
  }

  String buildMethod(MetaDataOrdering ordering) {
    StringBuilder sb = new StringBuilder(200);
    sb.append("  @DependencyMeta(type=\"").append(type).append("\"");
    if (name != null) {
      sb.append(", name=\"").append(name).append("\"");
    }
    if (hasMethod()) {
      sb.append(", method=\"").append(method).append("\"");
    }
    if (!provides.isEmpty()) {
      appendProvides(sb, "provides", provides);
    }
    if (!dependsOn.isEmpty()) {
      appendProvides(sb, "dependsOn", dependsOn);
    }
    sb.append(")").append(NEWLINE);
    sb.append("  protected void build_").append(getBuildName()).append("() {").append(NEWLINE);
    if (hasMethod()) {
      sb.append("    ").append(Util.shortMethod(method)).append("(builder");
    } else {
      sb.append("    ").append(shortType).append(Constants.DI).append(".build(builder");
    }

    for (String depend : dependsOn) {
      if (GenericType.isGeneric(depend) && !Util.isProvider(depend)) {
        // provide implementation of generic interface as a parameter to the build method
        final MetaData providerMeta = findProviderOf(depend, ordering);
        if (providerMeta != null) {
          final GenericType type = GenericType.parse(depend);
          sb.append(", ").append(providerMeta.shortType).append("$DI.provider").append(type.shortName()).append("(builder)");
        }
      }
    }
    sb.append(");").append(NEWLINE);
    sb.append("  }").append(NEWLINE);
    return sb.toString();
  }

  private MetaData findProviderOf(String depend, MetaDataOrdering ordering) {
    return ordering.findProviderOf(depend);
  }

  private boolean hasMethod() {
    return method != null && !method.isEmpty();
  }

  private void appendProvides(StringBuilder sb, String attribute, List<String> types) {
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
    this.dependsOn = dependsOn;
  }

  void setMethod(String method) {
    this.method = method;
  }

}
