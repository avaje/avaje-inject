package io.avaje.inject;

/**
 * A bean entry with priority and optional name.
 */
public class BeanEntry<T> {

  /**
   * An explicitly supplied bean. See BeanScopeBuilder.
   */
  public static final int SUPPLIED = 2;

  /**
   * An <code>@Primary</code> bean.
   */
  public static final int PRIMARY = 1;

  /**
   * A normal priority bean.
   */
  public static final int NORMAL = 0;

  /**
   * A <code>@Secondary</code> bean.
   */
  public static final int SECONDARY = -1;

  private final int priority;

  private final T bean;

  private final String name;

  /**
   * Construct with priority, name and the bean.
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

  /**
   * Return true if this entry has Supplied priority.
   */
  public boolean isSupplied() {
    return priority == SUPPLIED;
  }

  /**
   * Return true if this entry has Primary priority.
   */
  public boolean isPrimary() {
    return priority == PRIMARY;
  }

  /**
   * Return true if this entry has Secondary priority.
   */
  public boolean isSecondary() {
    return priority == SECONDARY;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{bean:").append(bean);
    if (name != null) {
      sb.append(", name:").append(name);
    }
    switch (priority) {
      case SUPPLIED:
        sb.append(", Supplied");
        break;
      case PRIMARY:
        sb.append(", @Primary");
        break;
      case SECONDARY:
        sb.append(", @Secondary");
        break;
      default:
        sb.append(", Normal");
    }
    return sb.append("}").toString();
  }
}
