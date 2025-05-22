package io.ebean;

/**
 * Simulate the Ebean io.ebean.Database interface with a shutdown() method.
 * <p>
 * For "Graceful Shutdown" we desire this to be shutdown last.
 */
public interface Database {

  /** Shutdown including underlying DataSources. We want this to occur LAST. */
  void shutdown();

  /** Test only method */
  boolean isShutdown();
}
