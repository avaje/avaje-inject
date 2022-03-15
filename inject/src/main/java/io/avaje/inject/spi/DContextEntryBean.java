package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import javax.inject.Provider;
import java.util.Objects;

/**
 * Holds either the bean itself or a provider of the bean.
 */
class DContextEntryBean {

  /**
   * Create taking into account if it is a Provider or the bean itself.
   *
   * @param bean The bean itself or provider of the bean
   * @param name The optional name for the bean
   * @param flag The flag for primary, secondary or normal
   */
  static DContextEntryBean of(Object bean, String name, int flag) {
    if (bean instanceof Provider) {
      return new Prov((Provider<?>)bean, name, flag);
    } else {
      return new DContextEntryBean(bean, name, flag);
    }
  }

  static DContextEntryBean provider(Provider<?> provider, String name, int flag) {
      return new Prov(provider, name, flag);
  }

  protected final Object source;
  private final String name;
  private final int flag;

  private DContextEntryBean(Object source, String name, int flag) {
    this.source = source;
    this.name = KeyUtil.lower(name);
    this.flag = flag;
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

  /**
   * Provider based entry - provider controls the scope of the provided bean.
   */
  static final class Prov extends DContextEntryBean {

    private final Provider<?> provider;

    private Prov(Provider<?> provider, String name, int flag) {
      super(provider, name, flag);
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
}
