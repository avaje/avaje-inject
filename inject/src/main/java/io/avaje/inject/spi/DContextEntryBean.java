package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import jakarta.inject.Provider;

/**
 * Holds either the bean itself or a provider of the bean.
 */
class DContextEntryBean {

  /**
   * Create taking into account if it is a Provider or the bean itself.
   */
  static DContextEntryBean of(Object source, String name, int priority, Class<? extends AvajeModule> currentModule) {
    if (source instanceof Provider) {
      return new ProtoProvider((Provider<?>)source, name, priority, currentModule);
    } else {
      return new DContextEntryBean(source, name, priority, currentModule);
    }
  }

  /**
   * Create an entry with supplied Providers using a 'Once' / 'one instance' provider.
   */
  static DContextEntryBean supplied(Object source, String name, int priority) {
    if (source instanceof Provider) {
      return new OnceBeanProvider((Provider<?>)source, name, priority, null);
    } else {
      return new DContextEntryBean(source, name, priority, null);
    }
  }

  static DContextEntryBean provider(boolean prototype, Provider<?> provider, String name, int flag, Class<? extends AvajeModule> currentModule) {
    return prototype ? new ProtoProvider(provider, name, flag, currentModule) : new OnceBeanProvider(provider, name, flag, currentModule);
  }

  protected final Object source;
  protected final String name;
  protected final Class<? extends AvajeModule> sourceModule;
  private final int priority;

  private DContextEntryBean(Object source, String name, int priority, Class<? extends AvajeModule> currentModule) {
    this.source = source;
    this.name = name;
    this.priority = priority;
    this.sourceModule = currentModule;
  }

  @Override
  public final String toString() {
    return "Bean{" +
      "source=" + source +
      ", name='" + name + '\'' +
      ", priority=" + priority +
      ", sourceModule=" + sourceModule +
      '}';
  }

  final DEntry entry() {
    return new DEntry(name, priority, bean());
  }

  /**
   * Return true if qualifierName is null or matched.
   */
  final boolean isNameMatch(String qualifierName) {
    return qualifierName == null || qualifierName.equalsIgnoreCase(name);
  }

  /**
   * Return true if qualifierName is matched including null.
   */
  final boolean isNameEqual(String qualifierName) {
    return qualifierName == null ? name == null : qualifierName.equalsIgnoreCase(name);
  }

  final Class<? extends AvajeModule> sourceModule() {
    return sourceModule;
  }

  /**
   * Return the bean if name matches and otherwise null.
   */
  Object beanIfNameMatch(String name) {
    return isNameMatch(name) ? bean() : null;
  }

  String name() {
    return name;
  }

  Object bean() {
    return source;
  }

  Provider<?> provider() {
    return this::bean;
  }

  final boolean isPrimary() {
    return priority == BeanEntry.PRIMARY;
  }

  final int priority() {
    return priority;
  }

  final boolean isSupplied(String qualifierName) {
    return priority == BeanEntry.SUPPLIED && (qualifierName == null || qualifierName.equals(name));
  }

  final Object nonDefaultMatch(String name) {
    return isNameMatch(name) && priority != BeanEntry.SECONDARY ? bean() : null;
  }

  /**
   * Prototype scope Provider based entry.
   */
  static final class ProtoProvider extends DContextEntryBean {

    private final Provider<?> provider;

    private ProtoProvider(Provider<?> provider, String name, int flag, Class<? extends AvajeModule> currentModule) {
      super(provider, name, flag, currentModule);
      this.provider = provider;
    }

    @Override
    Provider<?> provider() {
      return provider;
    }

    @Override
    Object bean() {
      return provider.get();
    }
  }

  /** Single instance scoped Provider based entry. */
  static final class OnceBeanProvider extends DContextEntryBean {

    private final Provider<?> provider;

    private OnceBeanProvider(Provider<?> provider, String name, int flag, Class<? extends AvajeModule> currentModule) {
      super(provider, name, flag, currentModule);
      this.provider = new OnceProvider<>(provider);
    }

    @Override
    Object bean() {
      return provider.get();
    }
  }
}
