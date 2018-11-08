package io.kanuka;

import io.kanuka.core.BeanContextFactory;
import io.kanuka.core.Builder;
import io.kanuka.core.BuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class BootContext {

  private static final Logger log = LoggerFactory.getLogger(BootContext.class);

  /**
   * Load all the named contexts.
   */
  public BeanContext load() {

    // sort factories by dependsOn
    FactoryOrder factoryOrder = new FactoryOrder();
    ServiceLoader.load(BeanContextFactory.class).forEach(factoryOrder::add);

    Set<String> moduleNames = factoryOrder.orderFactories();
    log.debug("building context with modules {}", moduleNames);

    Builder rootBuilder = BuilderFactory.newRootBuilder();

    for (BeanContextFactory factory : factoryOrder.factories()) {
      log.debug("loading context name:{}", factory.getName());
      rootBuilder.addChild(factory.createContext(rootBuilder));
    }

    // entire graph built, fire postConstruct
    log.debug("postConstruct firing on all beans");
    rootBuilder.postConstruct();
    return rootBuilder.build();
  }

  /**
   * Helper to order the BeanContextFactory based on dependsOn.
   */
  static class FactoryOrder {

    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<BeanContextFactory> factories = new ArrayList<>();
    private final List<BeanContextFactory> queue = new ArrayList<>();

    void add(BeanContextFactory factory) {
      String[] dependsOn = factory.getDependsOn();
      if (dependsOn == null || dependsOn.length == 0) {
        push(factory);
      } else {
        queue.add(factory);
      }
    }

    private void push(BeanContextFactory factory) {
      factories.add(factory);
      moduleNames.add(factory.getName());
    }

    /**
     * Order the factories returning the ordered list of module names.
     */
    Set<String> orderFactories() {
      processQueue();
      return moduleNames;
    }

    /**
     * Return the list of factories in the order they should be built.
     */
    List<BeanContextFactory> factories() {
      return factories;
    }

    /**
     * Process the queue pushing the factories in order to satisfy dependencies.
     */
    private void processQueue() {

      int count;
      do {
        count = processQueuedFactories();
      } while (count > 0);

      if (!queue.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (BeanContextFactory factory : queue) {
          sb.append("module "+factory.getName()+" has unsatisfied dependencies - ");
          for (String depModuleName : factory.getDependsOn()) {
            boolean ok = moduleNames.contains(depModuleName);
            String result = (ok) ? "ok" : "UNSATISFIED";
            sb.append(String.format("depends on %s - %s", depModuleName, result));
          }
        }

        sb.append("- Modules loaded ok ").append(moduleNames);
        throw new IllegalStateException(sb.toString());
      }
    }

    /**
     * Process the queued factories pushing them when all their (module) dependencies
     * are satisfied.
     *
     * This returns the number of factories added so once this returns 0 it is done.
     */
    private int processQueuedFactories() {

      int count = 0;
      Iterator<BeanContextFactory> it = queue.iterator();
      while (it.hasNext()) {
        BeanContextFactory factory = it.next();
        if (satisfiedDependencies(factory)) {
          it.remove();
          push(factory);
          count++;
        }
      }
      return count;
    }

    /**
     * Return true if the (module) dependencies are satisfied for this factory.
     */
    private boolean satisfiedDependencies(BeanContextFactory factory) {
      for (String moduleName : factory.getDependsOn()) {
        if (!moduleNames.contains(moduleName)){
          return false;
        }
      }
      return true;
    }
  }

}
