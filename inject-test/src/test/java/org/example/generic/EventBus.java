package org.example.generic;

public interface EventBus<E, S extends Subscriber<? extends E>> {}
