package org.example.coffee.primary;

import io.avaje.inject.SystemContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserOfPEmailerTest {

  @Test
  public void email() {

    UserOfPEmailer bean = SystemContext.getBean(UserOfPEmailer.class);
    assertThat(bean.email()).isEqualTo("primary");
  }
}
