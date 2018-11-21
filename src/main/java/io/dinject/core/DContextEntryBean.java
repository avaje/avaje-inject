package io.dinject.core;

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
   */
  public static DContextEntryBean of(Object bean, String name) {
    if (bean instanceof Provider) {
      return new DContextEntryBean.Prov(bean, name);
    } else {
      return new DContextEntryBean(bean, name);
    }
  }

  protected final Object source;

  private final String name;

  private DContextEntryBean(Object source, String name) {
    this.source = source;
    this.name = name;
  }

  boolean isNameMatch(String name) {
    return name.equals(this.name);
  }

  Object obtainInstance() {
    // its a plain bean, just return it
    return source;
  }

  Object getBean() {
    return obtainInstance();
  }

  Object getIfMatchWithDefault(String name) {
    if (name == null || Objects.equals(this.name, name)) {
      return obtainInstance();
    } else {
      return null;
    }
  }

  /**
   * Provider based entry - get it once.
   */
  static class Prov extends DContextEntryBean {

    private Object actualBean;

    private Prov(Object provider, String name) {
      super(provider, name);
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
