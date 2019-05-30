package io.dinject.core;

import org.mockito.Mockito;

import java.util.function.Consumer;

/**
 * Holds beans supplied to the dependency injection.
 * <p>
 * These are typically test doubles or mock instances that we supply in testing.
 * When we supply bean instances they take precedence over other beans that
 * would normally be injected.
 * </p>
 */
public class SuppliedBean<B> {

  private final Class<B> type;

  private final Consumer<B> consumer;

  private B bean;

  /**
   * Create with a given target type and bean instance.
   */
  public SuppliedBean(Class<B> type, B bean) {
    this(type, bean, null);
  }

  /**
   * Create with a consumer to setup the mock.
   */
  public SuppliedBean(Class<B> type, B bean, Consumer<B> consumer) {
    this.type = type;
    this.bean = bean;
    this.consumer = consumer;
  }

  /**
   * Return the dependency injection target type.
   */
  public Class<B> getType() {
    return type;
  }

  /**
   * Return the bean instance to use (often a test double or mock).
   */
  public B getBean() {
    if (bean == null) {
      // should extract a SPI for this
      bean = Mockito.mock(type);
    }
    if (consumer != null) {
      consumer.accept(bean);
    }
    return bean;
  }
}
