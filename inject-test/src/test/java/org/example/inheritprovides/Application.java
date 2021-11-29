package org.example.inheritprovides;

import javax.inject.Inject;
import javax.inject.Singleton;
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
