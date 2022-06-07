package org.example.generic;

import java.util.Optional;

public interface ReadService<T, KeyType> {
    Optional<T> get(KeyType id);
}
