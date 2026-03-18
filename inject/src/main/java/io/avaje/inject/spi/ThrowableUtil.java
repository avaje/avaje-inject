package io.avaje.inject.spi;

import java.util.function.Consumer;

public class ThrowableUtil {
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public static <E extends Throwable> E throwAnyway(Throwable throwable) throws E {
        throw (E) throwable;
    }

    public static Runnable guard(ThrowingRunnable action) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    action.run();
                } catch (Exception e) {
                    throw throwAnyway(e);
                }
            }
        };
    }

    public static <T> Consumer<T> guard(ThrowingConsumer<T> action) {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                try {
                    action.accept(t);
                } catch (Exception e) {
                    throw throwAnyway(e);
                }
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
