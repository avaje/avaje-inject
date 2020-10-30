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
    this.name = name;
    this.flag = flag;
  }

  boolean isNameMatch(String name) {
    return name == null || name.equals(this.name);
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

  boolean isSupplied() {
    return flag == BeanEntry.SUPPLIED;
  }

  @SuppressWarnings("unchecked")
  BeanEntry getBeanEntry() {
    return new BeanEntry(flag, getBean(), name);
  }

  @SuppressWarnings("unchecked")
  BeanEntry candidate(String name) {
    if (name == null || Objects.equals(this.name, name)) {
      return new BeanEntry(flag, obtainInstance(), name);
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
        actualBean = ((Provider) source).get();
      }
      return actualBean;
    }

  }
}
