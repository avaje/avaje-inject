package org.example.myapp.interweave;

import org.other.one.interweave.IFromLocal;

import io.avaje.inject.Component;

/** Local implementation of {@link IFromLocal} — satisfies the external module's dependency. */
@Component
public class LocalImpl implements IFromLocal {}
