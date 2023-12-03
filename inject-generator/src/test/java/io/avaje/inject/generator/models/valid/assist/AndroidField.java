package io.avaje.inject.generator.models.valid.assist;

import io.avaje.inject.Assist;
import io.avaje.inject.Assisted;
import io.avaje.inject.generator.models.valid._Wiring;
import jakarta.inject.Inject;

@Assisted
public class AndroidField {
  @Assist int personality;
  @Assist Model model;
  @Inject _Wiring wiring;
}
