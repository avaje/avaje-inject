package io.kanuka;

import java.util.List;

public class Context {

  static BeanContext rootContext = init();

  private static BeanContext init() {
    return new BootContext().load();
  }

  public static <T> T getBean(Class<T> cls) {
    return rootContext.getBean(cls);
  }

  public static <T> T getBean(Class<T> cls, String name) {
    return rootContext.getBean(cls, name);
  }

  public static List<Object> getBeans(Class<?> cls) {
    return rootContext.getBeans(cls);
  }

}
