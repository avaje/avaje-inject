package io.avaje.inject.spi;

import io.avaje.inject.BeanEntry;
import jakarta.inject.Named;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Holds beans supplied to the dependency injection.
 * <p>
 * These can be externally supplied dependencies or test doubles for testing purposes.
 */
public class SuppliedBean {

  private static final Class<?>[] NO_INTERFACES = new Class[0];

  private final String name;
  private final Type type;
  private final int priority;
  protected Object source;

  /**
   * Create with a class type and bean instance.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static SuppliedBean of(Class<?> type, Object source) {
    return new SuppliedBean.ForClass(null, type, source, null);
  }

  /**
   * Create for a class type with a consumer that runs once when the bean is obtained.
   */
  public static <B> SuppliedBean of(String name, Class<B> type, Consumer<B> consumer) {
    return new SuppliedBean.ForClass<>(name, type, null, consumer);
  }

  /**
   * Create for a class type with name.
   */
  public static <B> SuppliedBean of(String name, Class<B> type, B source) {
    return new SuppliedBean.ForClass<>(name, type, source, null);
  }

  /**
   * Create a supplied bean for a generic type.
   */
  public static SuppliedBean ofType(String name, Type type, Object source) {
    return new SuppliedBean(BeanEntry.SUPPLIED, name, type, source);
  }

  /**
   * Create a supplied bean with SECONDARY priority as a default fallback dependency that is
   * only used when no other matching one is provided.
   */
  public static SuppliedBean secondary(String name, Type type, Object source) {
    return new SuppliedBean(BeanEntry.SECONDARY, name, type, source);
  }

  private SuppliedBean(int priority, String name, Type type, Object source) {
    this.priority = priority;
    this.name = name;
    this.type = type;
    this.source = source;
  }

  /**
   * Return the bean instance or provider to use for injection.
   */
  public Object source() {
    return source;
  }

  /**
   * Return the associated priority.
   */
  public final int priority() {
    return priority;
  }

  /**
   * Return the dependency injection target type.
   */
  public final Type type() {
    return type;
  }

  /**
   * Return the qualifier name of the supplied bean.
   */
  public final String name() {
    if (name != null) {
      return name;
    }
    if (type instanceof Class<?>) {
      Named annotation = ((Class<?>) type).getAnnotation(Named.class);
      return (annotation == null) ? null : annotation.value();
    }
    return null;
  }

  /**
   * Return the interfaces to additionally register along with the type.
   */
  public final Class<?>[] interfaces() {
    if (type instanceof Class<?>) {
      return ((Class<?>) type).getInterfaces();
    }
    return NO_INTERFACES;
  }

  /**
   * Class based supplied bean.
   */
  private static final class ForClass<B> extends SuppliedBean {

    private final Consumer<B> consumer;
    private final Class<B> classType;

    ForClass(String name, Class<B> type, Object source, Consumer<B> consumer) {
      super(BeanEntry.SUPPLIED, name, type, source);
      this.classType = type;
      this.consumer = consumer;
    }

    @Override
    public Object source() {
      if (source == null) {
        var mock = Mockito.mock(classType);
        if (consumer != null) {
          consumer.accept(mock);
        }
        source = mock;
      }
      return source;
    }
  }
}
