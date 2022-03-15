package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import io.avaje.inject.BeanScope;
import jakarta.inject.Provider;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import static io.avaje.inject.spi.DBeanScope.combine;

class DBuilder implements Builder {

  /**
   * List of Lifecycle methods.
   */
  private final List<Runnable> postConstruct = new ArrayList<>();
  private final List<AutoCloseable> preDestroy = new ArrayList<>();

  /**
   * List of field injection closures.
   */
  private final List<Consumer<Builder>> injectors = new ArrayList<>();

  /**
   * The beans created and added to the scope during building.
   */
  protected final DBeanMap beanMap = new DBeanMap();

  private final BeanScope parent;
  private final boolean parentOverride;

  /**
   * Debug of the current bean being wired - used in injection errors.
   */
  private Type injectTarget;

  /**
   * Flag set when we are running post construct injection.
   */
  private boolean runningPostConstruct;

  DBuilder(BeanScope parent, boolean parentOverride) {
    this.parent = parent;
    this.parentOverride = parentOverride;
  }

  @Override
  public boolean isAddBeanFor(Type... types) {
    return isAddBeanFor(null, types);
  }

  @Override
  public boolean isAddBeanFor(String name, Type... types) {
    next(name, types);
    if (parentOverride || parent == null) {
      return true;
    }
    for (Type type : types) {
      try {
        parent.get(type, name);
        return false;
      } catch (NoSuchElementException e) {
        // ignore
      }
    }
    return true;
  }

  protected void next(String name, Type... types) {
    injectTarget = firstOf(types);
    beanMap.nextBean(name, types);
  }

  private Type firstOf(Type[] types) {
    return types != null && types.length > 0 ? types[0] : null;
  }

  @Override
  public <T> Set<T> set(Class<T> interfaceType) {
    return new LinkedHashSet<>(list(interfaceType));
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <T> List<T> list(Class<T> interfaceType) {
    List<T> values = (List<T>) beanMap.all(interfaceType);
    if (parent == null) {
      return values;
    }
    return combine(values, parent.list(interfaceType));
  }

  private <T> T getMaybe(Type type, String name) {
    T bean = beanMap.get(type, name);
    if (bean != null) {
      return bean;
    }
    return (parent == null) ? null : parent.get(type, name);
  }

  /**
   * Return the bean to register potentially with spy enhancement.
   */
  protected <T> T enrich(T bean, DBeanMap.NextBean next) {
    // only enriched by DBuilderExtn
    return bean;
  }

  @Override
  public <T> T register(T bean) {
    return register(BeanEntry.NORMAL, bean);
  }

  @Override
  public <T> T registerPrimary(T bean) {
    return register(BeanEntry.PRIMARY, bean);
  }

  @Override
  public <T> T registerSecondary(T bean) {
    return register(BeanEntry.SECONDARY, bean);
  }

  private <T> T register(int flag, T bean) {
    bean = enrich(bean, beanMap.next());
    beanMap.register(flag, bean);
    return bean;
  }

  @Override
  public <T> void registerProvider(Provider<T> provider) {
    // no enrichment
    beanMap.register(BeanEntry.NORMAL, provider);
  }

  @Override
  public <T> void withBean(Class<T> type, T bean) {
    next(null, type);
    beanMap.register(BeanEntry.SUPPLIED, bean);
  }

  @Override
  public void addPostConstruct(Runnable invoke) {
    postConstruct.add(invoke);
  }

  @Override
  public void addPreDestroy(AutoCloseable invoke) {
    preDestroy.add(invoke);
  }

  @Override
  public void addInjector(Consumer<Builder> injector) {
    injectors.add(injector);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls) {
    return getOptional(cls, null);
  }

  @Override
  public <T> Optional<T> getOptional(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    return Optional.ofNullable(bean);
  }

  @Override
  public <T> T getNullable(Class<T> cls) {
    return getNullable(cls, null);
  }

  @Override
  public <T> T getNullable(Class<T> cls, String name) {
    return getMaybe(cls, name);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> cls) {
    return getProvider(cls, null);
  }

  @Override
  public <T> Provider<T> getProvider(Class<T> cls, String name) {
    if (runningPostConstruct) {
      return obtainProvider(cls, name);
    }
    // use injectors to delay obtaining the provider until end of build
    ProviderPromise<T> promise = new ProviderPromise<>(cls, name, this);
    injectors.add(promise);
    return promise;
  }

  <T> Provider<T> obtainProvider(Class<T> type, String name) {
    Provider<T> provider = beanMap.provider(type, name);
    if (provider != null) {
      return provider;
    }
    return ()-> this.get(type, name);
  }

  @Override
  public <T> Provider<T> getProviderFor(Class<?> cls, Type type) {
    return () -> {
      T bean = getMaybe(cls, null);
      if (bean == null) {
        bean = getMaybe(type, null);
      }
      if (bean != null) {
        return bean;
      }
      String msg = "Unable to inject an instance for generic type " + type + " usually provided by " + cls + "?";
      throw new IllegalStateException(msg);
    };
  }

  @Override
  public <T> T get(Class<T> cls) {
    return get(cls, null);
  }

  @Override
  public <T> T get(Class<T> cls, String name) {
    T bean = getMaybe(cls, name);
    if (bean == null) {
      throw new IllegalStateException(errorInjectingNull(cls, name));
    }
    return bean;
  }

  private <T> String errorInjectingNull(Class<T> cls, String name) {
    String msg = "Injecting null for " + cls.getName();
    if (name != null) {
      msg += " name:" + name;
    }
    List<T> beanList = list(cls);
    msg += " when creating " + injectTarget + " - potential beans to inject: " + beanList;
    if (!beanList.isEmpty()) {
      msg += ". Check @Named or Qualifier being used";
    }
    msg += ". Check for missing module? [ missing beanScopeBuilder.withModules() ]";
    msg += ". If it is expected to be externally provided, missing beanScopeBuilder.withBean() ?";
    return msg;
  }

  private void runInjectors() {
    runningPostConstruct = true;
    for (Consumer<Builder> injector : injectors) {
      injector.accept(this);
    }
  }

  public BeanScope build(boolean withShutdownHook) {
    runInjectors();
    return new DBeanScope(withShutdownHook, preDestroy, postConstruct, beanMap, parent).start();
  }
}
