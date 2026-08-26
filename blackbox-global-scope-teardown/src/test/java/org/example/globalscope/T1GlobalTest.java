package org.example.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@InjectTest
class T1GlobalTest {

  @Inject ServerStatus serverStatus;

  @Test
  void mbeanIsRegistered() throws Exception {
    Object status = ManagementFactory.getPlatformMBeanServer()
      .getAttribute(new ObjectName(ServerStatus.OBJECT_NAME), "Status");
    assertEquals("OK", status);
  }
}
