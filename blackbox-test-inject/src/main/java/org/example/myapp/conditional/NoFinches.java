package org.example.myapp.conditional;

import io.avaje.inject.RequiresProperty;

@NoKiwi
@RequiresProperty(missing = "noFinches")
public @interface NoFinches {}
