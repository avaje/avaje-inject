package org.example.inheritprovides;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class Application {

    private final List<Controller> controllers;

    @Inject
    public Application(List<Controller> controllers) {
        this.controllers = controllers;
    }

    public List<Controller> getControllers() {
        return controllers;
    }

}
