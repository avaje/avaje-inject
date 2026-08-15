package org.multi.globalscope;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Stands in for a JVM-global registry (quartz DBConnectionManager, JDBC DriverManager, an MBean server). */
public final class GlobalRegistry {
  public static final ConcurrentMap<String, String> CONNECTIONS = new ConcurrentHashMap<>();
  private GlobalRegistry() {}
}
