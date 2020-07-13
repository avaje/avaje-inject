package org.example.coffee;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.example.coffee.qualifier.SomeStore;
import org.example.coffee.secondary.SEmailer;
import org.example.coffee.secondary.Widget;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

public class ExtensionExampleTest {

  @Test
  public void checkForCompilerWarningsOnly_notATestThatRuns() {

    ExtensionExample extensionExample =  new ExtensionExample(asList(Widget.class, SEmailer.class), asList(SomeStore.class));
    BeanContext context = extensionExample.build();

    Class cls0 = Widget.class;
    Class<?> cls1 = SEmailer.class;

    BootContext bootContext = new BootContext()
      .withSpy(cls0)
      .withSpy(cls1)
      .withMock(cls0)
      .withMock(cls1);

    assertNotNull(context);
    assertNotNull(bootContext);
  }
}
