package io.avaje.inject.spi;

import java.util.function.Consumer;

public final class ThrowableUtil {

  private ThrowableUtil() {
  }

  @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
  private static <E extends Throwable> E throwAnyway(Throwable throwable) throws E {
    throw (E) throwable;
  }

  public static Runnable guard(ThrowingRunnable action) {
    return () -> {
      try {
        action.run();
      } catch (Exception e) {
        throw throwAnyway(e);
      }
    };
  }

  public static <T> Consumer<T> guard(ThrowingConsumer<T> action) {
    return t -> {
      try {
        action.accept(t);
      } catch (Exception e) {
        throw throwAnyway(e);
      }
    };
  }

  public interface ThrowingRunnable {
    void run() throws Exception;
  }

  public interface ThrowingConsumer<T> {
    void accept(T arg) throws Exception;
  }
}
