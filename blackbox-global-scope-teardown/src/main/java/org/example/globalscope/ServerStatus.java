package org.example.globalscope;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.ObjectName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

/**
 * Publishes a management view of this component on the JVM-wide platform MBeanServer,
 * the standard JMX pattern (HikariCP pools, Kafka clients, Jetty, and the JDK itself).
 */
@Singleton
public class ServerStatus implements ServerStatusMBean {

  public static final String OBJECT_NAME = "org.example.globalscope:type=ServerStatus";

  @PostConstruct
  void register() {
    try {
      ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName(OBJECT_NAME));
    } catch (JMException e) {
      throw new IllegalStateException("failed to register " + OBJECT_NAME, e);
    }
  }

  @PreDestroy
  void unregister() {
    try {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(OBJECT_NAME));
    } catch (JMException e) {
      throw new IllegalStateException("failed to unregister " + OBJECT_NAME, e);
    }
  }

  @Override
  public String getStatus() {
    return "OK";
  }
}
