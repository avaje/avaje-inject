package io.avaje.inject;

import java.util.function.Supplier;

public interface Invocation {

  void invoke() throws Throwable;

  final class Run implements Invocation {

    private final Runnable delegate;

    public Run(Runnable delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() {
      delegate.run();
    }
  }

  final class Call<T> implements Invocation {

    private final Supplier<T> delegate;
    private T result;

    public Call(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() {
      result = delegate.get();
    }

    public T result() {
      return result;
    }
  }

  final class CheckedCall<T> implements Invocation {

    private final CheckedSupplier<T> delegate;
    private T result;

    public CheckedCall(CheckedSupplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void invoke() throws Throwable {
      result = delegate.get();
    }

    public T result() {
      return result;
    }
  }

  interface CheckedSupplier<T> {

    T get() throws Throwable;
  }
}
