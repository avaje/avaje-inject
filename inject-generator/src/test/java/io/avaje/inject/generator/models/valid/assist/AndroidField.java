package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assisted;
import io.avaje.inject.AssistFactory;
import io.avaje.inject.generator.models.valid._Wiring;
import jakarta.inject.Inject;

@AssistFactory
public class AndroidField {
  @Assisted int personality;
  @Assisted Model model;
  @Inject _Wiring wiring;
}
