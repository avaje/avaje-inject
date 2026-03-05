package org.example.myapp.assist.css;

import io.avaje.inject.AssistFactory;
import io.avaje.inject.Assisted;
import jakarta.inject.Inject;

import java.awt.Component;

import org.example.myapp.assist.CssFactory;
import org.example.myapp.assist.Scanner;
import org.example.myapp.assist.Somthin;
import org.jspecify.annotations.Nullable;

@AssistFactory(CssFactory.class)
class CssScanner implements Scanner {

  private final String path;
  private final Somthin somthin;
  @Inject @Nullable Component comp;

  CssScanner(@Assisted String path, Somthin somthin) {
    this.path = path;
    this.somthin = somthin;
  }

  @Override
  public String scan() {
    return "scanWith|path=" + path + " | somethin=" + somthin.hi();
  }
}
