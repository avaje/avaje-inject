package org.example.myapp.generic;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Aldrich<T, T2> extends BiConsumer<T, T2>, Consumer<BiConsumer<T, T2>> {}
