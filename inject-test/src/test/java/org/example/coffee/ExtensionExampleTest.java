package org.example.coffee;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import org.example.coffee.qualifier.SomeStore;
import org.example.coffee.secondary.SEmailer;
import org.example.coffee.secondary.Widget;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExtensionExampleTest {

  @Test
  public void checkForCompilerWarningsOnly_notATestThatRuns() {

    ExtensionExample extensionExample = new ExtensionExample(asList(Widget.class, SEmailer.class), asList(SomeStore.class));
    BeanScope context = extensionExample.build();

    Class cls0 = Widget.class;
    Class<?> cls1 = SEmailer.class;

    BeanScopeBuilder bootContext = BeanScope.newBuilder()
      .withSpy(cls0)
      .withSpy(cls1)
      .withMock(cls0)
      .withMock(cls1);

    assertNotNull(context);
    assertNotNull(bootContext);
  }
}
