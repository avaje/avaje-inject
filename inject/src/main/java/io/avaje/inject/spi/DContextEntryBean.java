package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;

import jakarta.inject.Provider;
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
  public static DContextEntryBean of(Object bean, String name, int flag) {
    if (bean instanceof Provider) {
      return new DContextEntryBean.Prov(bean, name, flag);
    } else {
      return new DContextEntryBean(bean, name, flag);
    }
  }

  final Object source;
  private final String name;
  private final int flag;

  private DContextEntryBean(Object source, String name, int flag) {
    this.source = source;
    this.name = name == null ? null : name.toLowerCase();
    this.flag = flag;
  }

  /**
   * Return true if qualifierName is null or matched.
   */
  boolean isNameMatch(String qualifierName) {
    return qualifierName == null || qualifierName.equals(name);
  }

  /**
   * Return true if qualifierName is matched including null.
   */
  boolean isNameEqual(String qualifierName) {
    return Objects.equals(qualifierName, name);
  }

  Object obtainInstance() {
    // its a plain bean, just return it
    return source;
  }

  Object getBean() {
    return obtainInstance();
  }

  boolean isPrimary() {
    return flag == BeanEntry.PRIMARY;
  }

  boolean isSecondary() {
    return flag == BeanEntry.SECONDARY;
  }

  boolean isSupplied(String qualifierName) {
    return flag == BeanEntry.SUPPLIED && (qualifierName == null || qualifierName.equals(name));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  BeanEntry getBeanEntry() {
    return new BeanEntry(flag, getBean(), name);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  BeanEntry candidate(String qualifierName) {
    if (qualifierName == null || Objects.equals(name, qualifierName)) {
      return new BeanEntry(flag, obtainInstance(), qualifierName);
    } else {
      return null;
    }
  }

  /**
   * Provider based entry - get it once.
   */
  static class Prov extends DContextEntryBean {

    private Object actualBean;

    private Prov(Object provider, String name, int flag) {
      super(provider, name, flag);
    }

    @Override
    Object obtainInstance() {
      // its a provider, get it once
      if (actualBean == null) {
        actualBean = ((Provider<?>) source).get();
      }
      return actualBean;
    }

  }
}
