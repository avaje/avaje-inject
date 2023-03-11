package org.example.myapp.conditional;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;

@RequiresBean(missing = Kiwi.class)
@RequiresProperty(missing = "secondary")
public @interface NoKiwi {}
