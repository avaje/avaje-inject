package org.example.myapp.assist.css;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Named;
import org.example.myapp.assist.JsFactory;
import org.example.myapp.assist.MyJsName;
import org.example.myapp.assist.Scanner;
import org.example.myapp.assist.Somthin;

@MyJsName
@AssistFactory(JsFactory.class)
class JsWithQualScanner implements Scanner {

  private final String path;
  private final Somthin somthin;

  JsWithQualScanner(@Assisted String path, Somthin somthin) {
    this.path = path;
    this.somthin = somthin;
  }

  @Override
  public String scan() {
    return "jsScanQual|path=" + path + " | somethin=" + somthin.hi();
  }
}
