package org.example.myapp.assist.css;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Named;
import org.example.myapp.assist.*;

@Named("withName")
@AssistFactory(JsFactory.class)
class JsWithNameScanner implements Scanner {

  private final String path;
  private final Somthin somthin;

  JsWithNameScanner(@Assisted String path, Somthin somthin) {
    this.path = path;
    this.somthin = somthin;
  }

  @Override
  public String scan() {
    return "jsScanNamed|path=" + path + " | somethin=" + somthin.hi();
  }
}
