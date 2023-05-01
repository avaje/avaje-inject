package io.avaje.inject.aop;

import java.lang.reflect.Method;
import java.util.Arrays;

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
   * Return the arguments additionally appending the throwable.
   */
  Object[] arguments(Throwable e);

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
    public Object[] arguments(Throwable e) {
      if (args == null || args.length == 0) {
        return new Object[]{e};
      } else {
        Object[] newArgs = Arrays.copyOf(args, args.length + 1);
        newArgs[args.length] = e;
        return newArgs;
      }
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
  }

  /**
   * Runnable based Invocation.
   */
  final class Run extends Base<Void> {

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
    public Base<Void> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Run(() -> methodInterceptor.invoke(this))
        .with(instance, method, args);
    }

  }

  /**
   * Callable based Invocation with checked exceptions.
   */
  final class Call<T> extends Base<T> {

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
    public Base<T> wrap(MethodInterceptor methodInterceptor) {
      return new Invocation.Call<T>(() -> {
        final Call<T> delegate = this;
        methodInterceptor.invoke(delegate);
        return delegate.finalResult();
      }).with(instance, method, args);
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
