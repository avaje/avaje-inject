package io.avaje.inject.aop;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Method invocation used in {@link MethodInterceptor#invoke(Invocation)} for Aspects.
 * <p>
 * Represents a method invocation that can be intercepted with additional before and after
 * invocation logic.
 */
public interface Invocation {

  /**
   * Invoke the underlying method returning the result.
   *
   * @return The result of the method call. This will return null for void methods.
   * @throws Throwable Exception thrown by underlying method
   */
  Object invoke() throws Throwable;

  /**
   * Invoke the underlying method returning the result. Checked exceptions will be caught and
   * rethrown as {@code InvocationException}s.
   *
   * @return The result of the method call. This will return null for void methods.
   */
  default Object invokeUnchecked() {
    try {
      return invoke();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Throwable e) {
      throw new InvocationException(e);
    }
  }

  /**
   * Set the result that will be returned to the caller.
   * <p>
   * This will replace a prior result set by calling {@code #invoke} or can be used
   * to provide a result allowing to skip calling {@code #invoke} altogether.
   *
   * @param result The result that will be returned to the caller.
   */
  void result(Object result);

  /**
   * Return the arguments used for this invocation.
   */
  Object[] arguments();

  /**
   * Return the method being called for this invocation.
   */
  Method method();

  /**
   * Return the 'this' instance of the invocation.
   * <p>
   * This is typically used when invoking fallback/recovery methods.
   */
  Object instance();

  /**
   * Return whether this invocation has a registered recovery method
   */
  boolean hasRecoveryMethod();

  /**
   * Invoke the recovery method associated for this invocation and return the result.
   *
   * @return The result of the method call. This will return null for void methods.
   * @throws IllegalStateException if no fallback method is configured with this invocation
   */
  Object invokeRecoveryMethod(Throwable t);

  /**
   * Invocation base type for both callable and runnable methods.
   *
   * @param <T> The result type
   */
  abstract class Base<T> implements Invocation {

    protected Method method;
    protected Object[] args;
    protected Object instance;
    protected T result;

    /**
     * Set the instance, method and arguments for the invocation.
     */
    public Base<T> with(Object instance, Method method, Object... args) {
      this.instance = instance;
      this.method = method;
      this.args = args;
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void result(Object result) {
      this.result = (T) result;
    }

    /**
     * Return the final invocation result.
     */
    public T finalResult() {
      return result;
    }

    @Override
    public Object[] arguments() {
      return args;
    }

    @Override
    public Method method() {
      return method;
    }

    @Override
    public Object instance() {
      return instance;
    }

    /**
     * Wrap this invocation using a methodInterceptor returning the wrapped call.
     * <p>
     * This invocation is effectively nested inside the returned invocation.
     *
     * @param methodInterceptor The method interceptor to use to wrap this call with
     * @return The wrapped call
     */
    public abstract Base<T> wrap(MethodInterceptor methodInterceptor);

    protected void noRecovery(Object recover) {
      if (recover == null) {
        throw new IllegalStateException("No recovery method available for this invocation");
      }
    }
  }

  /**
   * Runnable based Invocation.
   */
  final class Run extends Base<Void> {

    private final CheckedRunnable delegate;
    private Consumer<Throwable> fallback;

    /**
     * Create with a given closure to run.
     */
    public Run(CheckedRunnable delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object invoke() throws Throwable {
      delegate.invoke();
      return null;
    }

    /**
     * Register a fallback method which can be used to recover from an exception
     * while intercepting an invocation
     */
    public Run fallback(Consumer<Throwable> fallback) {
      this.fallback = fallback;
      return this;
    }

    @Override
    public Base<Void> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Run(() -> methodInterceptor.invoke(this)).with(instance, method, args);
    }

    @Override
    public boolean hasRecoveryMethod() {
      return fallback != null;
    }

    @Override
    public Object invokeRecoveryMethod(Throwable t) {
      noRecovery(fallback);
      fallback.accept(t);
      return null;
    }

    @Override
    public Run with(Object instance, Method method, Object... args) {
      super.with(instance, method, args);
      return this;
    }
  }

  /**
   * Callable based Invocation with checked exceptions.
   */
  final class Call<T> extends Base<T> {

    private final CheckedSupplier<T> delegate;
    private Function<Throwable, T> fallback;

    /**
     * Create with a given supplier.
     */
    public Call(CheckedSupplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object invoke() throws Throwable {
      result = delegate.invoke();
      return result;
    }

    @Override
    public T finalResult() {
      return result;
    }

    @Override
    public Call<T> with(Object instance, Method method, Object... args) {
      super.with(instance, method, args);
      return this;
    }

    /**
     * register a fallback method which can be used to recover from an exception while intercepting
     * an invocation
     */
    public Call<T> fallback(Function<Throwable, T> fallback) {
      this.fallback = fallback;
      return this;
    }

    @Override
    public Base<T> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Call<>(() -> {
        final Call<T> delegate = this;
        methodInterceptor.invoke(delegate);
        return delegate.finalResult();
      }).with(instance, method, args);
    }

    @Override
    public boolean hasRecoveryMethod() {
      return fallback != null;
    }

    @Override
    public Object invokeRecoveryMethod(Throwable t) {
      noRecovery(fallback);
      var result = fallback.apply(t);
      super.result(result);
      return result;
    }
  }

  /**
   * Runnable with checked exceptions.
   */
  @FunctionalInterface
  interface CheckedRunnable {

    /**
     * Invoke the method.
     */
    void invoke() throws Throwable;
  }

  /**
   * Callable with checked exceptions.
   *
   * @param <T> The result type
   */
  @FunctionalInterface
  interface CheckedSupplier<T> {

    /**
     * Invoke the method returning the result.
     */
    T invoke() throws Throwable;
  }
}
