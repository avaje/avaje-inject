package org.example.myapp.foo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Main {

//  interface If {
//    void originalMethod(String s);
//  }

  static class Original {//implements If {
    public void originalMethod(String s) {
      System.out.println(s);
    }
  }

  static class Handler implements InvocationHandler {
    private final Original original;

    public Handler(Original original) {
      this.original = original;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      System.out.println("BEFORE "+proxy+" orig"+original);
      Object result = method.invoke(original, args);
      System.out.println("AFTER");
      return result;
    }
  }

  public static void main(String[] args){
    Original original = new Original();
    Handler handler = new Handler(original);
    Original f = (Original) Proxy.newProxyInstance(Original.class.getClassLoader(), new Class[] { Original.class }, handler);
    f.originalMethod("Hallo");
  }
}
