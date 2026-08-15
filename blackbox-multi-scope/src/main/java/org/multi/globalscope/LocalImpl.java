package org.multi.globalscope;

import jakarta.inject.Singleton;
import org.other.one.interweave.IFromLocal;

/**
 * Local implementation of {@link IFromLocal} — satisfies the dependency that OneModule (from
 * blackbox-other) declares on the local module. Without it the default scope cannot be wired.
 */
@Singleton
public class LocalImpl implements IFromLocal {}
