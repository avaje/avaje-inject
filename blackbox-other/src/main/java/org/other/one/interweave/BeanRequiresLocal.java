package org.other.one.interweave;

import io.avaje.inject.*;

/**
 * Bean from the external module that requires {@link IFromLocal}, which is implemented in the
 * local module. This creates the bidirectional relationship at the module level:
 * <ul>
 *   <li>the local module depends on {@link BeanFromOther} from this module</li>
 *   <li>this module's {@link BeanRequiresLocal} depends on {@link IFromLocal} from the local module</li>
 * </ul>
 * With {@code strictWiring = true} the local module's generated class interweaves all beans
 * in dependency order so both directions are satisfied without any circular bean dependency.
 */
@Component
public class BeanRequiresLocal {

  private final IFromLocal iFromLocal;

  public BeanRequiresLocal(@External IFromLocal iFromLocal) {
    this.iFromLocal = iFromLocal;
  }

  public IFromLocal iFromLocal() {
    return iFromLocal;
  }
}
