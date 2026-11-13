package org.example.myapp.autofactory.pkg;

public interface ProductMapper {

  String findProduct(String id);
  interface Search {
    String search(String term);
  }
}
