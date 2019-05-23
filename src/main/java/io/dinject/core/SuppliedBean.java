package io.dinject.core;

/**
 * Holds beans supplied to the dependency injection.
 * <p>
 * These are typically test doubles or mock instances that we supply in testing.
 * When we supply bean instances they take precedence over other beans that
 * would normally be injected.
 * </p>
 */
public class SuppliedBean {

  private final Class<?> type;

  private final Object bean;

  /**
   * Create with a given target type and bean instance.
   */
  public SuppliedBean(Class<?> type, Object bean) {
    this.type = type;
    this.bean = bean;
  }

  /**
   * Return the dependency injection target type.
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Return the bean instance to use (often a test double or mock).
   */
  public Object getBean() {
    return bean;
  }
}
