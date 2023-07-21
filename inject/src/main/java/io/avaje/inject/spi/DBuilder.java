package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import jakarta.inject.Provider;

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import static io.avaje.inject.spi.DBeanScope.combine;

class DBuilder implements Builder {

  private final PropertyRequiresPlugin propertyRequires;
  private final Set<String> profiles;
  /** List of Lifecycle methods. */
  private final List<Runnable> postConstruct = new ArrayList<>();

  private final List<Consumer<BeanScope>> postConstructConsumers = new ArrayList<>();
  private final List<AutoCloseable> preDestroy = new ArrayList<>();
  /** List of field injection closures. */
  private final List<Consumer<Builder>> injectors = new ArrayList<>();
  /** The beans created and added to the scope during building. */
  protected final DBeanMap beanMap = new DBeanMap();

  protected final BeanScope parent;
  protected final boolean parentOverride;
  /** Bean provided by the parent scope that we are not overriding. */
  protected Object parentMatch;
  /** Debug of the current bean being wired - used in injection errors. */
  private Type injectTarget;
  /** Flag set when we are running post construct injection. */
  private boolean runningPostConstruct;

  private DBeanScopeProxy beanScopeProxy;

  DBuilder(PropertyRequiresPlugin propertyRequires, BeanScope parent, boolean parentOverride) {
    this.propertyRequires = propertyRequires;
    this.parent = parent;
    this.parentOverride = parentOverride;
    this.profiles =
        propertyRequires.get("avaje.profiles").map(p -> p.split(",")).stream()
            .flatMap(Arrays::stream)
            .collect(toSet());
  }

  @Override
  public final boolean isAddBeanFor(Type... types) {
    return isAddBeanFor(null, types);
  }

  @Override
  public boolean isAddBeanFor(String name, Type... types) {
    parentMatch = null;
    next(name, types);
    if (parentOverride || parent == null) {
      return true;
    }
    if (parent instanceof DBeanScope) {
      // effectively looking for a match in the test scope
      final DBeanScope dParent = (DBeanScope) parent;
      parentMatch = dParent.getStrict(name, removeAnnotations(types));
      return parentMatch == null;
    }
    return true;
  }

  /**
   * Return the types without any annotation types.
   *
   * <p>For the purposes of supplied beans (typically test doubles) we are not interested in
   * annotation types.
   */
  protected final Type[] removeAnnotations(Type[] source) {
    for (int i = 1, end = source.length; i < end; i++) {
      if (isAnnotationType(source[i])) {
        // the annotation types are always at the tail so just return leading types
        return Arrays.copyOf(source, i);
      }
    }
    return source;
  }

  private boolean isAnnotationType(Type type) {
    return type instanceof Class && ((Class<?>) type).isAnnotation();
  }

  protected final void next(String name, Type... types) {
    injectTarget = firstOf(types);
    beanMap.nextBean(name, types);
  }

  private Type firstOf(Type[] types) {
    return types != null && types.length > 0 ? types[0] : null;
  }

  @Override
  public final <T> Set<T> set(Class<T> type) {
    return new LinkedHashSet<>(listOf(type));
  }

  @Override
  public final <T> List<T> list(Class<T> type) {
    return listOf(type);
  }

  @Override
  public final <T> Set<T> set(Type type) {
    return new LinkedHashSet<>(listOf(type));
  }

  @Override
  public final <T> List<T> list(Type type) {
    return listOf(type);
  }

  @SuppressWarnings({"unchecked"})
  private <T> List<T> listOf(Type type) {
    final List<T> values = (List<T>) beanMap.all(type);
    if (parent == null) {
      return values;
    }
    return combine(values, parent.list(type));
  }

  @Override
  public final <T> Map<String, T> map(Class<T> type) {
    return mapOf(type);
  }

  @Override
  public final <T> Map<String, T> map(Type type) {
    return mapOf(type);
  }

  @SuppressWarnings("unchecked")
  private <T> Map<String, T> mapOf(Type type) {
    return (Map<String, T>) beanMap.map(type, parent);
  }

  private <T> T getMaybe(Type type, String name) {
    final T bean = beanMap.get(type, name);
    if (bean != null) {
      return bean;
    }
    return parent == null ? null : parent.<T>getOptional(type, name).orElse(null);
  }

  /** Return the bean to register potentially with spy enhancement. */
  protected <T> T enrich(T bean, DBeanMap.NextBean next) {
    // only enriched by DBuilderExtn
    return bean;
  }

  @Override
  public final <T> T register(T bean) {
    bean = enrich(bean, beanMap.next());
    beanMap.register(bean);
    return bean;
  }

  @Override
  public Builder asPrimary() {
    beanMap.nextPriority(BeanEntry.PRIMARY);
    return this;
  }

  @Override
  public Builder asSecondary() {
    beanMap.nextPriority(BeanEntry.SECONDARY);
    return this;
  }

  @Override
  public Builder asPrototype() {
    beanMap.nextPrototype();
    return this;
  }

  @Override
  public final <T> void registerProvider(Provider<T> provider) {
    // no enrichment
    beanMap.register(provider);
  }

  @Override
  public final <T> void withBean(Class<T> type, T bean) {
    next(null, type);
    beanMap.nextPriority(BeanEntry.SUPPLIED);
    beanMap.register(bean);
  }

  @Override
  public final void addPostConstruct(Runnable invoke) {
    postConstruct.add(invoke);
  }

  @Override
  public void addPostConstruct(Consumer<BeanScope> consumer) {
    postConstructConsumers.add(consumer);
  }

  @Override
  public final void addPreDestroy(AutoCloseable invoke) {
    preDestroy.add(invoke);
  }

  @Override
  public final void addAutoClosable(Object maybeAutoCloseable) {
    if (maybeAutoCloseable instanceof AutoCloseable) {
      preDestroy.add((AutoCloseable) maybeAutoCloseable);
    }
  }

  @Override
  public final void addInjector(Consumer<Builder> injector) {
    injectors.add(injector);
  }

  @Override
  public final <T> Optional<T> getOptional(Class<T> type) {
    return optional(type, null);
  }

  @Override
  public final <T> Optional<T> getOptional(Class<T> type, String name) {
    return optional(type, name);
  }

  @Override
  public final <T> Optional<T> getOptional(Type type) {
    return optional(type, null);
  }

  @Override
  public final <T> Optional<T> getOptional(Type type, String name) {
    return optional(type, name);
  }

  private <T> Optional<T> optional(Type type, String name) {
    return Optional.ofNullable(getMaybe(type, name));
  }

  @Override
  public final <T> T getNullable(Class<T> type) {
    return getMaybe(type, null);
  }

  @Override
  public final <T> T getNullable(Class<T> type, String name) {
    return getMaybe(type, name);
  }

  @Override
  public final <T> T getNullable(Type type) {
    return getMaybe(type, null);
  }

  @Override
  public final <T> T getNullable(Type type, String name) {
    return getMaybe(type, name);
  }

  @Override
  public final <T> Provider<T> getProvider(Class<T> type) {
    return provider(type, null);
  }

  @Override
  public final <T> Provider<T> getProvider(Class<T> type, String name) {
    return provider(type, name);
  }

  @Override
  public final <T> Provider<T> getProvider(Type type) {
    return provider(type, null);
  }

  @Override
  public final <T> Provider<T> getProvider(Type type, String name) {
    return provider(type, name);
  }

  private <T> Provider<T> provider(Type type, String name) {
    if (runningPostConstruct) {
      return obtainProvider(type, name);
    }
    // use injectors to delay obtaining the provider until end of build
    final ProviderPromise<T> promise = new ProviderPromise<>(type, name, this);
    injectors.add(promise);
    return promise;
  }

  <T> Provider<T> obtainProvider(Type type, String name) {
    final Provider<T> provider = beanMap.provider(type, name);
    if (provider != null) {
      return provider;
    }
    return () -> this.get(type, name);
  }

  @Override
  public final <T> Provider<T> getProviderFor(Class<?> cls, Type type) {
    return () -> {
      T bean = getMaybe(cls, null);
      if (bean == null) {
        bean = getMaybe(type, null);
      }
      if (bean != null) {
        return bean;
      }
      final String msg =
          "Unable to inject an instance for generic type "
              + type
              + " usually provided by "
              + cls
              + "?";
      throw new IllegalStateException(msg);
    };
  }

  @Override
  public final <T> T get(Class<T> type) {
    return getBean(type, null);
  }

  @Override
  public final <T> T get(Class<T> type, String name) {
    return getBean(type, name);
  }

  @Override
  public final <T> T get(Type type) {
    return getBean(type, null);
  }

  @Override
  public final <T> T get(Type type, String name) {
    return getBean(type, name);
  }

  @Override
  public boolean containsProfiles(List<String> type) {

    return !Collections.disjoint(profiles, type);
  }

  @Override
  public boolean containsAllProfiles(List<String> type) {
    for (final var string : type) {
      if (!profiles.contains(string)) return false;
    }
    return true;
  }

  @Override
  public boolean contains(String type) {
    return beanMap.contains(type);
  }

  @Override
  public boolean contains(Type type) {
    return beanMap.contains(type);
  }

  @Override
  public boolean containsQualifier(String name) {
    return beanMap.containsQualifier(name);
  }

  @Override
  public PropertyRequiresPlugin property() {
    return propertyRequires;
  }

  private <T> T getBean(Type type, String name) {
    if (BeanScope.class.equals(type)) {
      return injectBeanScope();
    }
    final T bean = getMaybe(type, name);
    if (bean == null) {
      throw new IllegalStateException(errorInjectingNull(type, name));
    }
    return bean;
  }

  @SuppressWarnings("unchecked")
  private <T> T injectBeanScope() {
    if (beanScopeProxy == null) {
      beanScopeProxy = new DBeanScopeProxy(this);
    }
    return (T) beanScopeProxy;
  }

  private <T> String errorInjectingNull(Type type, String name) {
    final StringBuilder msg = new StringBuilder("Injecting null for ").append(type.getTypeName());
    if (name != null) {
      msg.append(" name:").append(name);
    }
    final List<T> beanList = list(type);
    msg.append(" when creating ")
        .append(injectTarget)
        .append(" - potential beans to inject: ")
        .append(beanList);
    if (!beanList.isEmpty()) {
      msg.append(". Check @Named or Qualifier being used");
    }
    msg.append(". Check for missing module? [ missing beanScopeBuilder.modules() ]");
    msg.append(". If it is expected to be externally provided, missing beanScopeBuilder.bean() ?");
    return msg.toString();
  }

  private void runInjectors() {
    runningPostConstruct = true;
    for (final Consumer<Builder> injector : injectors) {
      injector.accept(this);
    }
  }

  @Override
  public final BeanScope build(boolean withShutdownHook, long start) {
    runInjectors();
    final var scope =
        new DBeanScope(
            withShutdownHook, preDestroy, postConstruct, postConstructConsumers, beanMap, parent);
    if (beanScopeProxy != null) {
      beanScopeProxy.inject(scope);
    }
    return scope.start(start);
  }
}
