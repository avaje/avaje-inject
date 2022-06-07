package org.example.generic;

public interface CRUDService<T, KeyType> extends ReadService<T, KeyType>, CreateService<T, KeyType> {

  String iamCrud();
}
