package io.avaje.inject.aop;

/**
 * Exception occurring during method interception.
 * <p>
 * When using aspects and {@link MethodInterceptor} any throwable that is undeclared on the
 * method is caught and re-thrown as an InvocationException.
 */
public class InvocationException extends RuntimeException {

  /**
   * Create with a given cause.
   */
  public InvocationException(String message) {
    super(message);
  }

  /**
   * Create with a given cause.
   */
  public InvocationException(Throwable cause) {
    super(cause);
  }

  /**
   * Create with a given message and cause.
   */
  public InvocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
