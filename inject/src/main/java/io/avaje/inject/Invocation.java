package io.avaje.inject;

import java.lang.reflect.Method;

/**
 * Method invocation using in {@link MethodInterceptor#invoke(Invocation)} for Aspects.
 * <p>
 * Represents a method invocation that can be intercepted with additional before and after
 * invocation logic.
 */
public interface Invocation {

  /**
   * Invoke the underlying method returning the result.
   * <p>
   * This will return null for void methods.
   */
  Object invoke() throws Throwable;

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
   * Builds Invocation for both callable and runnable methods.
   *
   * @param <T> The result type
   */
  abstract class Build<T> implements Invocation {

    protected Method method;
    protected Object[] args;
    protected T result;

    /**
     * Set the method for the invocation.
     */
    public Build<T> method(Method method) {
      this.method = method;
      return this;
    }

    /**
     * Set the arguments for the invocation.
     */
    public Build<T> arguments(Object... args) {
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

    /**
     * Wrap this invocation using a methodInterceptor returning the wrapped call.
     * <p>
     * This invocation is effectively nested inside the returned invocation.
     *
     * @param methodInterceptor The method interceptor to use to wrap this call with
     * @return The wrapped call
     */
    public abstract Build<T> wrap(MethodInterceptor methodInterceptor);
  }

  /**
   * Runnable based Invocation.
   */
  final class Run extends Build<Void> {

    private final CheckedRunnable delegate;

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

    @Override
    public Build<Void> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Run(() -> methodInterceptor.invoke(this))
        .arguments(args)
        .method(method);
    }

  }

  /**
   * Callable based Invocation with checked exceptions.
   */
  final class Call<T> extends Build<T> {

    private final CheckedSupplier<T> delegate;

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
    public Build<T> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Call<T>(() -> {
        final Call<T> delegate = this;
        methodInterceptor.invoke(delegate);
        return delegate.finalResult();
      })
        .arguments(args)
        .method(method);
    }
  }

  /**
   * Runnable with checked exceptions.
   */
  @FunctionalInterface
  interface CheckedRunnable {

    void invoke() throws Throwable;
  }

  /**
   * Callable with checked exceptions.
   *
   * @param <T> The result type
   */
  @FunctionalInterface
  interface CheckedSupplier<T> {

    T invoke() throws Throwable;
  }
}
