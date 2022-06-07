package org.example.generic;

public interface CreateService<T, KeyType> {

  KeyType create(T bean);
}
