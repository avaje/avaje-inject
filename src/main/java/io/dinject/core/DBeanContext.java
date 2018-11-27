package io.dinject.core;

import io.dinject.BeanContext;
import io.dinject.BeanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DBeanContext implements BeanContext {

  private static final Logger log = LoggerFactory.getLogger(DBeanContext.class);

  private final String name;

  private final String[] provides;

  private final String[] dependsOn;

  private final List<BeanLifecycle> lifecycleList;

  private final DBeanMap beans;

  private final Map<String, BeanContext> children;

  private boolean closed;

  DBeanContext(String name, String[] provides, String[] dependsOn, List<BeanLifecycle> lifecycleList, DBeanMap beans, Map<String, BeanContext> children) {
    this.name = name;
    this.provides = provides;
    this.dependsOn = dependsOn;
    this.lifecycleList = lifecycleList;
    this.beans = beans;
    this.children = children;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getProvides() {
    return provides;
  }

  @Override
  public String[] getDependsOn() {
    return dependsOn;
  }

  @Override
  public <T> T getBean(Class<T> beanClass) {
    return getBean(beanClass, null);
  }

  @Override
  public <T> BeanEntry<T> candidate(Class<T> type, String name) {

    // sort candiates by priority - Primary, Normal, Secondary
    EntrySort<T> entrySort = new EntrySort<>();

    entrySort.add(beans.candidate(type, name));
    for (BeanContext childContext : children.values()) {
      entrySort.add(childContext.candidate(type, name));
    }
    return entrySort.get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getBean(Class<T> beanClass, String name) {

    BeanEntry<T> candidate = candidate(beanClass, name);
    return (candidate == null) ? null : candidate.getBean();
  }

  @Override
  public <T> List<T> getBeans(Class<T> interfaceType) {
    List<T> list = new ArrayList<>();
    beans.addAll(interfaceType, list);
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeans(interfaceType));
    }
    return list;
  }

  @Override
  public List<Object> getBeansWithAnnotation(Class<?> annotation) {

    List<Object> list = new ArrayList<>();
    beans.addAll(annotation, list);
    for (BeanContext childContext : children.values()) {
      list.addAll(childContext.getBeansWithAnnotation(annotation));
    }
    return list;
  }

  @Override
  public void start() {
    synchronized (this) {
      if (name != null) {
        log.debug("firing postConstruct on beans in context:{}", name);
      }
      for (BeanLifecycle bean : lifecycleList) {
        bean.postConstruct();
      }
      for (BeanContext childContext : children.values()) {
        childContext.start();
      }
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!closed) {
        // we only allow one call to preDestroy
        closed = true;
        if (name != null) {
          log.debug("firing preDestroy on beans in context:{}", name);
        }
        for (BeanLifecycle bean : lifecycleList) {
          bean.preDestroy();
        }
        for (BeanContext childContext : children.values()) {
          childContext.close();
        }
      }
    }
  }

  private static class EntrySort<T> {

    private BeanEntry<T> primary;
    private int primaryCount;
    private BeanEntry<T> secondary;
    private int secondaryCount;
    private BeanEntry<T> normal;
    private int normalCount;

    void add(BeanEntry<T> candidate) {

      if (candidate != null) {
        if (candidate.getPriority() == Flag.PRIMARY) {
          primary = candidate;
          primaryCount++;
        } else if (candidate.getPriority() == Flag.SECONDARY) {
          secondary = candidate;
          secondaryCount++;
        } else {
          normal = candidate;
          normalCount++;
        }
      }
    }

    BeanEntry<T> get() {
      if (primaryCount > 1) {
        throw new IllegalStateException("Multiple @Primary beans when only expecting one? Last was: " + primary);
      }
      if (primaryCount == 1) {
        return primary;
      }
      if (normalCount > 1) {
        throw new IllegalStateException("Multiple beans when only expecting one? Maybe use @Primary or @Secondary? Last was: " + normal);
      }
      if (normalCount == 1) {
        return normal;
      }
      if (secondaryCount > 1) {
        throw new IllegalStateException("Multiple @Secondary beans when only expecting one? Last was: " + primary);
      }
      return secondary;
    }
  }
}
