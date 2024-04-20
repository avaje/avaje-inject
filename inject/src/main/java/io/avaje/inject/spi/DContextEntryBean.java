package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import jakarta.inject.Provider;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds either the bean itself or a provider of the bean.
 */
class DContextEntryBean {

  /** Create taking into account if it is a Provider or the bean itself. */
  static DContextEntryBean of(Object source, String name, int flag, Class<? extends Module> currentModule) {
    if (source instanceof Provider) {
      return new ProtoProvider((Provider<?>) source, name, flag, currentModule);
    } else {
      return new DContextEntryBean(source, name, flag, currentModule);
    }
  }

  /** Create an entry with supplied Providers using a 'Once' / 'one instance' provider. */
  static DContextEntryBean supplied(Object source, String name, int flag) {
    if (source instanceof Provider) {
      return new OnceProvider((Provider<?>) source, name, flag, null);
    } else {
      return new DContextEntryBean(source, name, flag, null);
    }
  }

  static DContextEntryBean provider(boolean prototype, Provider<?> provider, String name, int flag, Class<? extends Module> sourceModule2) {
    return prototype
        ? new ProtoProvider(provider, name, flag, sourceModule2)
        : new OnceProvider(provider, name, flag, sourceModule2);
  }

  protected final Object source;
  protected final String name;
  private final int flag;
  private final Class<? extends Module> sourceModule;

  private DContextEntryBean(Object source, String name, int flag, Class<? extends Module> currentModule) {
    this.source = source;
    this.name = KeyUtil.lower(name);
    this.flag = flag;
    this.sourceModule = currentModule;
  }

  @Override
  public final String toString() {
    return "Bean{" +
      "source=" + source +
      ", name='" + name + '\'' +
      ", flag=" + flag +
      '}';
  }

  final DEntry entry() {
    return new DEntry(name, flag, bean());
  }

  /**
   * Return true if qualifierName is null or matched.
   */
  final boolean isNameMatch(String qualifierName) {
    return qualifierName == null || qualifierName.equals(name);
  }

  /**
   * Return true if qualifierName is matched including null.
   */
  final boolean isNameEqual(String qualifierName) {
    return Objects.equals(qualifierName, name);
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
    return flag == BeanEntry.PRIMARY;
  }

  final boolean isSecondary() {
    return flag == BeanEntry.SECONDARY;
  }

  final boolean isSupplied() {
    return flag == BeanEntry.SUPPLIED;
  }

  final boolean isSupplied(String qualifierName) {
    return flag == BeanEntry.SUPPLIED && (qualifierName == null || qualifierName.equals(name));
  }

  final Class<? extends Module> sourceModule() {
    return sourceModule;
  }

  /** Prototype scope Provider based entry. */
  static final class ProtoProvider extends DContextEntryBean {

    private final Provider<?> provider;

    private ProtoProvider(
        Provider<?> provider, String name, int flag, Class<? extends Module> currentModule) {
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

  /**
   * Single instance scoped Provider based entry.
   */
  static final class OnceProvider extends DContextEntryBean {

    private final ReentrantLock lock = new ReentrantLock();
    private final Provider<?> provider;
    private Object bean;

    private OnceProvider(Provider<?> provider, String name, int flag, Class<? extends Module> sourceModule) {
      super(provider, name, flag, sourceModule);
      this.provider = provider;
    }

    @Override
    Object bean() {
      lock.lock();
      try {
        if (bean == null) {
          bean = provider.get();
        }
        return bean;
      } finally {
        lock.unlock();
      }
    }
  }
}
