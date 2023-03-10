package org.example.myapp.conditional;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;

@RequiresBean(Bird.class)
@RequiresProperty(value = "watcher", equalTo = "bird")
public @interface RequiresBird {}
