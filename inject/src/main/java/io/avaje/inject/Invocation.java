package io.avaje.inject;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public interface Invocation {

  /**
   * Invoke the underlying method.
   */
  void invoke() throws Throwable;

  /**
   * Return the result of {@code #invoke}. This will be null for void methods.
   * <p>
   * If invoke is called multiple times (e.g. a retry mechanism) then this returns
   * the result of the last successful call to {@code #invoke}.
   */
  Object result();

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
   * Builds Invocation.
   *
   * @param <T> The result type
   */
  abstract class Build<T> implements Invocation {

    private Method method;
    private Object[] args;
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

    @Override
    public Object result() {
      return result;
    }

    @Override
    public void result(Object result) {
      this.result = (T) result;
    }

    /**
     * Return the invocation result. This is null for method invocations returning void.
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
  }

  /**
   * Runnable based Invocation.
   */
  final class Run extends Build<Void> {

    private final Runnable delegate;

    public Run(Runnable delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() {
      delegate.run();
    }
  }

  /**
   * Callable based Invocation.
   */
  final class Call<T> extends Build<T> {

    private final Supplier<T> delegate;

    public Call(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() {
      result = delegate.get();
    }

    @Override
    public T finalResult() {
      return result;
    }
  }

  /**
   * Callable based Invocation with checked exceptions.
   */
  final class CheckedCall<T> extends Build<T> {

    private final CheckedSupplier<T> delegate;
    private T result;

    public CheckedCall(CheckedSupplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() throws Throwable {
      result = delegate.get();
    }

    @Override
    public T finalResult() {
      return result;
    }
  }

  /**
   * Supplier with checked exceptions.
   *
   * @param <T> The result type
   */
  interface CheckedSupplier<T> {

    T get() throws Throwable;
  }
}
