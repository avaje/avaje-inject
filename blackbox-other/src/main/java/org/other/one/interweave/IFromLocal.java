package org.other.one.interweave;

/**
 * Interface defined in the external module but implemented by the local module.
 * This allows the external module to declare a dependency on something from the local
 * module without creating a compile-time circular Maven dependency.
 */
public interface IFromLocal {}
