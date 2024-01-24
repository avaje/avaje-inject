package org.example.myapp.assist.droid;

import java.util.List;

public interface CarFactory {

  Car create(Paint paint, List<String> type);
}
