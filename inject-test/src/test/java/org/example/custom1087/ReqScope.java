package org.example.custom1087;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requiresString = "org.example.custom1087.ReqHolder<org.example.custom1087.Req>")
public @interface ReqScope {
}
