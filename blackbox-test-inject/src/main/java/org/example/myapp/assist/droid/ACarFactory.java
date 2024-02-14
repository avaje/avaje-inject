package org.example.myapp.assist.droid;

import java.util.List;

public abstract class ACarFactory {

  public abstract ACar construct(Paint paint, int size, List<String> type);

  public void nonFactory() {}
}
