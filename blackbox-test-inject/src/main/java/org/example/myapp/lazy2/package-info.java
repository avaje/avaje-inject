/**
 * Use Lazy for all the beans in this package.
 * <p>
 * Use {@code enforceProxy = true} to fail compilation if there is no default constructor/lazy not supported.
 */
@Lazy(enforceProxy = true)
package org.example.myapp.lazy2;

import io.avaje.inject.Lazy;
