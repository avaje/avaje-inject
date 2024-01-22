package org.example.myapp.assist;

import io.avaje.inject.Component;
import jakarta.inject.Named;

@Component
public class JsThing {

  final JsFactory nameFactory;
  final JsFactory qualFactory;

  public JsThing(@Named("withName") JsFactory nameFactory, @MyJsName JsFactory qualFactory) {
    this.nameFactory = nameFactory;
    this.qualFactory = qualFactory;
  }

  public String nameScan(String input) {
    var scanner = nameFactory.scanner(input);
    return scanner.scan();
  }

  public String qualScan(String input) {
    var scanner = qualFactory.scanner(input);
    return scanner.scan();
  }
}
