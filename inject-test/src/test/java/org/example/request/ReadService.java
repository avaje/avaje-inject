package org.example.request;

import java.util.Optional;

public interface ReadService<T, KeyType> {
    Optional<T> get(KeyType id);
}
