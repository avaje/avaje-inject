package org.example.myapp.generic;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class GenericImplTest {

  @Inject
  GenericInterfaceObject<Flow.Publisher<Object>> flow;

  @Test
  void test() {
    Flow.Publisher<Object> publisher = flow.get();
    assertThat(publisher).isNotNull();
    assertThat(publisher).isInstanceOf(SubmissionPublisher.class);
  }
}
