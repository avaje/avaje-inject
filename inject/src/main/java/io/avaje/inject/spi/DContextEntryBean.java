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

  protected final Object source;
  private Object proxyBean;
  private final String name;
  private final int flag;
  private final boolean proxy;

  private DContextEntryBean(Object source, String name, int flag) {
    this.source = source;
    this.name = KeyUtil.lower(name);
    this.flag = flag;
    this.proxy = source.getClass().getAnnotation(Proxy.class) != null;
  }

  @Override
  public String toString() {
    return "Bean{" +
      "source=" + source +
      ", name='" + name + '\'' +
      ", flag=" + flag +
      ", proxy=" + proxy +
      '}';
  }

  DEntry entry() {
    return new DEntry(name, flag, getBean());
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

  Object getBean() {
    return proxyBean != null ? proxyBean : source;
  }

  boolean isPrimary() {
    return flag == BeanEntry.PRIMARY;
  }

  boolean isProxy() {
    return proxy;
  }

  boolean isSecondary() {
    return flag == BeanEntry.SECONDARY;
  }

  boolean isSupplied() {
    return flag == BeanEntry.SUPPLIED;
  }

  boolean isSupplied(String qualifierName) {
    return flag == BeanEntry.SUPPLIED && (qualifierName == null || qualifierName.equals(name));
  }

  boolean isProxiedBy(DContextEntryBean proxyEntry) {
    if (source.getClass() != proxyEntry.source.getClass().getSuperclass()) {
      return false;
    } else {
      proxyBean = proxyEntry.source;
      return true;
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
    Object getBean() {
      // its a provider, get it once
      if (actualBean == null) {
        actualBean = ((Provider<?>) source).get();
      }
      return actualBean;
    }

  }
}
