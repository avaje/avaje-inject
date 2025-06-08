package io.avaje.inject.generator.models.valid.generic;

import jakarta.inject.Singleton;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Singleton
public class GenericImpl implements GenericInterfaceObject<Flow.Publisher<Object>> {
    public Flow.Publisher<Object> get() {
        return new SubmissionPublisher();
    }
}
