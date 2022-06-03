package io.avaje.inject.generator;

import java.util.Stack;

final class GenericTypeParser {

  private final String raw;
  private StringBuilder buf = new StringBuilder();
  private final Stack<GenericType> stack = new Stack<>();

  GenericTypeParser(String raw) {
    this.raw = raw;
    stack.push(new GenericType(raw));
  }

  GenericType parse() {
    for (int i = 0, len = raw.length(); i < len; i++) {
      processChar(raw.charAt(i));
    }
    return stack.peek();
  }

  private void processChar(char ch) {
    switch (ch) {
      case '<':
        startParam();
        break;
      case '>':
        endParam();
        break;
      case ',':
        nextParam();
        break;
      default:
        buf.append(ch);
    }
  }

  private void nextParam() {
    endParam();

    GenericType param = new GenericType();
    stack.peek().addParam(param);
    stack.push(param);
  }

  private void endParam() {
    String content = buf.toString();
    if (!content.isEmpty()) {
      stack.peek().setMainType(content);
      buf = new StringBuilder();
    }
    stack.pop();
  }

  private void startParam() {
    stack.peek().setMainType(buf.toString());
    buf = new StringBuilder();

    GenericType param = new GenericType();
    stack.peek().addParam(param);
    stack.push(param);
  }
}
