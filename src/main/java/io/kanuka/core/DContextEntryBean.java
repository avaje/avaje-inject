package io.kanuka.core;

import java.util.Objects;

class DContextEntryBean {

  final Object bean;
  final String name;

  DContextEntryBean(Object bean, String name) {
    this.bean = bean;
    this.name = name;
  }

  boolean isNameMatch(String name) {
    return name.equals(this.name);
  }

  public Object getBean() {
    return bean;
  }

  public Object getIfMatch(String name) {
    if (Objects.equals(this.name, name)) {
      return bean;
    } else {
      return null;
    }
  }

  public Object getIfMatchWithDefault(String name) {
    if (name == null || Objects.equals(this.name, name)) {
      return bean;
    } else {
      return null;
    }
  }
}
