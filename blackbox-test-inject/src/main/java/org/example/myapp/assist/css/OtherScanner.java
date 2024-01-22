package org.example.myapp.assist.css;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import org.example.myapp.assist.Scanner;
import org.example.myapp.assist.Somthin;

@AssistFactory
class OtherScanner implements Scanner {

  private final String path;
  private final Somthin somthin;

  OtherScanner(@Assisted String path, Somthin somthin) {
    this.path = path;
    this.somthin = somthin;
  }

  @Override
  public String scan() {
    return "otherScanner|path=" + path + " | somethin=" + somthin.hi();
  }
}
