package io.avaje.inject.spi;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

/**
 * Map of types (class types, interfaces and annotations) to a DContextEntry where the
 * entry holds a list of bean instances for that type.
 */
final class DBeanMap {
  private static final Optional<Object> EMPTY = Optional.empty();
  private final Map<String, DContextEntry> beans = new LinkedHashMap<>();
  private final Set<String> qualifiers = new HashSet<>();

  private NextBean nextBean;
  private Class<? extends AvajeModule> currentModule;
  private Set<String> forScopes = Set.of();

  DBeanMap() {
  }

  void currentModule(Class<? extends AvajeModule> currentModule) {
    this.currentModule = currentModule;
  }

  void setCurrentScopes(@Nullable Set<String> scopes) {
    if (scopes == null) {
      this.forScopes = Set.of();
    } else {
      this.forScopes = scopes;
    }
  }

  @Override
  public String toString() {
    return "BeanMap{" + beans + '}';
  }

  /**
   * Add to the map of entries.
   */
  void addAll(Map<DContextEntryBean, DEntry> map) {
    for (Map.Entry<String, DContextEntry> entry : beans.entrySet()) {
      for (DContextEntryBean contentEntry : entry.getValue().entries()) {
        map.computeIfAbsent(contentEntry, dContextEntryBean -> contentEntry.entry()).addKey(entry.getKey());
      }
    }
  }

  /**
   * Add test double supplied beans.
   */
  void add(List<SuppliedBean> suppliedBeans) {
    for (SuppliedBean suppliedBean : suppliedBeans) {
      addSuppliedBean(suppliedBean);
    }
  }

  private void addSuppliedBean(SuppliedBean supplied) {
    Type suppliedType = supplied.type();
    qualifiers.add(supplied.name());
    DContextEntryBean entryBean = DContextEntryBean.supplied(supplied.source(), supplied.name(), supplied.priority());
    beans.computeIfAbsent(suppliedType.getTypeName(), s -> new DContextEntry()).add(entryBean);
    if (!suppliedType.getTypeName().startsWith("java.lang")) {
      for (Class<?> anInterface : supplied.interfaces()) {
        beans.computeIfAbsent(anInterface.getTypeName(), s -> new DContextEntry()).add(entryBean);
      }
    }
  }

  void register(Object bean) {
    if (bean == null || EMPTY.equals(bean)) {
      return;
    }
    var name = nextBean.name;
    qualifiers.add(name);
    var entryBean = DContextEntryBean.of(bean, name, nextBean.priority, currentModule);
    for (Type type : nextBean.types) {
      beans.computeIfAbsent(type.getTypeName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  void register(Provider<?> provider) {
    qualifiers.add(nextBean.name);
    var entryBean = DContextEntryBean.provider(nextBean.prototype, provider, nextBean.name, nextBean.priority, currentModule);
    for (Type type : nextBean.types) {
      beans.computeIfAbsent(type.getTypeName(), s -> new DContextEntry()).add(entryBean);
    }
  }

  /**
   * Get with a strict match on name for the single entry case.
   */
  Object getStrict(Type type, String name) {
    DContextEntry entry = beans.get(type.getTypeName());
    if (entry == null) {
      return null;
    }
    return entry.getStrict(name);
  }

  boolean contains(String type) {
    return beans.containsKey(type);
  }

  boolean contains(Type type) {
    return beans.containsKey(type.getTypeName());
  }

  boolean containsQualifier(String type) {
    return qualifiers.contains(type);
  }

  @SuppressWarnings("unchecked")
  <T> T get(Type type, String name) {
    DContextEntry entry = beans.get(type.getTypeName());
    if (entry == null) {
      return null;
    }
    return (T) entry.get(name, currentModule);
  }

  public <T> List<T> listByPriority(Type type) {

    DContextEntry entry = beans.get(type.getTypeName());
    if (entry == null) {
      return List.of();
    }

    return entry.entries().stream()
        .sorted(Comparator.comparingInt(DContextEntryBean::priority))
        .map(e -> (T) e.bean())
        .collect(toList());
  }

  @SuppressWarnings("unchecked")
  <T> Provider<T> provider(Type type, String name) {
    DContextEntry entry = beans.get(type.getTypeName());
    if (entry == null) {
      return null;
    }
    return (Provider<T>) entry.provider(name, currentModule);
  }

  /**
   * Return all bean instances matching the given type and name.
   */
  List<Object> all(Type type, @Nullable String name) {
    DContextEntry entry = beans.get(type.getTypeName());
    return entry != null ? entry.all(name) : List.of();
  }

  /**
   * Return a map of bean instances keyed by qualifier name.
   */
  Map<String, Object> map(Type type, BeanScope parent) {
    if (parent == null) {
      return map(type);
    }
    Map<String, Object> parentMap = parent.map(type);
    Map<String, Object> localMap = map(type);
    if (parentMap.isEmpty()) {
      return localMap;
    } else if (localMap.isEmpty()) {
      return parentMap;
    }
    Map<String, Object> result = new LinkedHashMap<>(parentMap);
    result.putAll(localMap);
    return result;
  }

  private Map<String, Object> map(Type type) {
    DContextEntry entry = beans.get(type.getTypeName());
    return entry != null ? entry.map() : Map.of();
  }

  /**
   * Return true if there is a supplied bean for the name and types.
   */
  boolean isSupplied(String qualifierName, Type... types) {
    if (types != null) {
      for (Type type : types) {
        DContextEntry entry = beans.get(type.getTypeName());
        if (entry != null) {
          DContextEntryBean suppliedBean = entry.supplied(qualifierName);
          if (suppliedBean != null) {
            if (types.length > 1) {
              addSuppliedFor(type, types, suppliedBean);
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Register the suppliedBean entry with parameterized types IF those types
   * don't already have a registered entry.
   */
  private void addSuppliedFor(Type matchType, Type[] types, DContextEntryBean suppliedBean) {
    for (Type type : types) {
      if (type != matchType && type instanceof ParameterizedType) {
        beans.computeIfAbsent(type.getTypeName(), s -> new DContextEntry()).add(suppliedBean);
      }
    }
  }

  /**
   * Store the qualifier name and type for the next bean to register.
   */
  void nextBean(String name, Type[] types) {
    nextBean = new NextBean(name, types);
  }

  /**
   * Set the priority for the next bean to register.
   */
  void nextPriority(int priority) {
    nextBean.priority = priority;
  }

  /**
   * Set the next bean to register as having Prototype scope.
   */
  void nextPrototype() {
    nextBean.prototype = true;
  }

  /**
   * Return the types of the bean being processed/registered.
   */
  NextBean next() {
    return nextBean;
  }

  /**
   * List current scope annotations
   */
  Set<String> scopeAnnotations() {
    return forScopes;
  }

  static class NextBean {
    final String name;
    final Type[] types;
    int priority = BeanEntry.NORMAL;
    boolean prototype;

    NextBean(String name, Type[] types) {
      this.name = name;
      this.types = types;
    }
  }
}
