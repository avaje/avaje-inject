package org.example.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Same test as T1, back on the shared global scope. Fails after T2: closing the
 * failed mock scope ran the duplicate ServerStatus preDestroy, which unregistered
 * (by ObjectName) the MBean the still-live global scope instance owns.
 */
@InjectTest
class T3GlobalAgainTest {

  @Inject ServerStatus serverStatus;

  @Test
  void mbeanStillRegistered() throws Exception {
    Object status = ManagementFactory.getPlatformMBeanServer()
      .getAttribute(new ObjectName(ServerStatus.OBJECT_NAME), "Status");
    assertEquals("OK", status);
  }
}
