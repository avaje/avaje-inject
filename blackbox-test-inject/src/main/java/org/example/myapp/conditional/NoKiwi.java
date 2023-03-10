package org.example.myapp.conditional;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;

@RequiresBean(missingBeans = Kiwi.class)
@RequiresProperty(missingProperties = "secondary")
public @interface NoKiwi {}
