package org.example.myapp.assist.driod;

import java.util.List;

public interface CarFactory {

  Car create(Paint paint, List<String> type);
}
