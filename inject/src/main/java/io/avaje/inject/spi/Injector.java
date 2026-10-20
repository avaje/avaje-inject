package io.avaje.inject.spi;

/**
 * Injects dependencies into an instance of {@code T} that avaje-inject did
 * not construct itself, for example an instance created by an external
 * framework.
 *
 * <p>A generated implementation of this interface is registered into the
 * scope for any type annotated with {@code @io.avaje.inject.InjectorTarget}.
 * If the type has a {@code @PreDestroy} method, the generated injector
 * remembers every instance passed to {@link #inject(Object)} and invokes it
 * on each of them automatically when the scope closes.
 *
 * <h3>Example</h3>
 *
 * <pre>{@code
 * Injector<LegacyServlet> injector = scope.get(new GenericType<Injector<LegacyServlet>>() {});
 * injector.inject(servlet);
 * }</pre>
 *
 * @param <T> the type being injected into
 */
public interface Injector<T> {

  /** Inject the dependencies into the given bean. */
  void inject(T bean);
}
