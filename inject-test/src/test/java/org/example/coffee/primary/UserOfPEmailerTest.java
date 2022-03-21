package org.example.coffee.primary;

import io.avaje.inject.xtra.ApplicationScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserOfPEmailerTest {

  @Test
  public void email() {

    UserOfPEmailer bean = ApplicationScope.get(UserOfPEmailer.class);
    assertThat(bean.email()).isEqualTo("primary");
  }
}
