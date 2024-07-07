package io.avaje.inject.spi;

import io.avaje.spi.Service;

/**
 * Superclass for all Inject SPI classes
 *
 * <p>Add extensions by implementing one of the following extension interfaces and have it
 * registered with the {@link java.util.ServiceLoader ServiceLoader} as an {@link InjectExtension}.
 *
 * <h4>Available Extensions</h4>
 *
 * <ul>
 *   <li>{@link AvajeModule}
 *   <li>{@link ConfigPropertyPlugin}
 *   <li>{@link InjectPlugin}
 *   <li>{@link ModuleOrdering}
 * </ul>
 */
@Service
public interface InjectExtension {}
