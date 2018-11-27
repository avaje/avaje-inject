package io.dinject;

/**
 * A bean entry in the context with priority and optional name.
 */
public class BeanEntry<T> {

  private final int priority;

  private final T bean;

  private final String name;

  /**
   * Construct with priorty, name and the bean.
   */
  public BeanEntry(int priority, T bean, String name) {
    this.priority = priority;
    this.bean = bean;
    this.name = name;
  }

  /**
   * Return the priority (Primary, Normal and Secondary).
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Return the bean.
   */
  public T getBean() {
    return bean;
  }

  /**
   * Return the bean name.
   */
  public String getName() {
    return name;
  }
}
