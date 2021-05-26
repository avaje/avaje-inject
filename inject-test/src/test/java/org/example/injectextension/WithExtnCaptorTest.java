package org.example.injectextension;

import io.avaje.inject.test.InjectExtension;
import javax.inject.Inject;
import org.example.coffee.fruit.AppleService;
import org.example.coffee.fruit.PeachService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(InjectExtension.class)
class WithExtnCaptorTest {

  ArgumentCaptor<String> plainCaptor = ArgumentCaptor.forClass(String.class);

  @Captor
  ArgumentCaptor<String> injectedCaptor;

  @Mock
  PeachService peachService;
  @Inject
  AppleService appleService;

  @Test
  void verifyCaptor_when_plainCaptor() {
    appleService.passIt("MyArg");

    verify(peachService).callIt(plainCaptor.capture());
    assertThat(plainCaptor.getValue()).isEqualTo("MyArg");
  }

  @Test
  void verifyCaptor_when_injectedCaptor() {
    appleService.passIt("MyOtherArg");

    verify(peachService).callIt(injectedCaptor.capture());
    assertThat(injectedCaptor.getValue()).isEqualTo("MyOtherArg");
  }

}
