package io.avaje.inject.spi;

import org.mockito.Mockito;

import jakarta.inject.Named;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Holds beans supplied to the dependency injection.
 * <p>
 * These can be externally supplied dependencies or test doubles for testing purposes.
 */
public abstract class SuppliedBean<B> {

  protected final String name;
  protected final Type type;
  protected B bean;

  /**
   * Create with a class type and bean instance.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static SuppliedBean of(Class<?> type, Object bean) {
    return new SuppliedBean.ForClass(null, type, bean, null);
  }

  /**
   * Create for a class type with a consumer that runs once when the bean is obtained.
   */
  public static <B> SuppliedBean<B> of(String name, Class<B> type, B bean, Consumer<B> consumer) {
    return new SuppliedBean.ForClass<>(name, type, bean, consumer);
  }

  /**
   * Create for a class type with name.
   */
  public static <B> SuppliedBean<B> of(String name, Class<B> type, B bean) {
    return new SuppliedBean.ForClass<>(name, type, bean, null);
  }

  /**
   * Create a supplied bean for a generic type.
   */
  public static <B> SuppliedBean<B> ofType(String name, Type type, B bean) {
    return new SuppliedBean.ForType<>(name, type, bean);
  }

  private SuppliedBean(String name, Type type, B bean) {
    this.name = name;
    this.type = type;
    this.bean = bean;
  }

  private SuppliedBean(String name, Class<B> type, B bean) {
    this.name = initName(name, type);
    this.type = type;
    this.bean = bean;
  }


  String initName(String name, Class<?> type) {
    if (name != null) {
      return name;
    }
    Named annotation = type.getAnnotation(Named.class);
    return (annotation == null) ? null : annotation.value();
  }

  /**
   * Return the dependency injection target type.
   */
  public Type type() {
    return type;
  }

  /**
   * Return the qualifier name of the supplied bean.
   */
  public String name() {
    return name;
  }

  /**
   * Return the bean instance to use for injection.
   */
  public abstract B bean();

  /**
   * Return the interfaces to additionally register along with the type.
   */
  public abstract Class<?>[] interfaces();

  /**
   * Type based supplied bean.
   */
  private static class ForType<B> extends SuppliedBean<B> {

    private ForType(String name, Type type, B bean) {
      super(name, type, bean);
    }

    @Override
    public B bean() {
      return bean;
    }

    @Override
    public Class<?>[] interfaces() {
      return new Class[0];
    }
  }

  /**
   * Class based supplied bean.
   */
  private static class ForClass<B> extends SuppliedBean<B> {

    private final Consumer<B> consumer;
    private final Class<B> classType;

    ForClass(String name, Class<B> type, B bean, Consumer<B> consumer) {
      super(name, type, bean);
      this.classType = type;
      this.consumer = consumer;
    }

    @Override
    public B bean() {
      if (bean == null) {
        // should extract a SPI for this
        bean = Mockito.mock(classType);
      }
      if (consumer != null) {
        consumer.accept(bean);
      }
      return bean;
    }

    @Override
    public Class<?>[] interfaces() {
      return classType.getInterfaces();
    }
  }
}
