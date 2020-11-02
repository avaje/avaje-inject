package io.avaje.inject.spi;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanEntry;
import io.avaje.inject.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
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
  public <T> List<T> getBeansByPriority(Class<T> interfaceType) {
    return getBeansByPriority(interfaceType, Priority.class);
  }

  @Override
  public <T> List<T> getBeansByPriority(Class<T> interfaceType, Class<? extends Annotation> priorityAnnotation) {
    List<T> list = getBeans(interfaceType);
    return list.size() > 1 ? sortByPriority(list, priorityAnnotation) : list;
  }

  @Override
  public <T> List<T> sortByPriority(List<T> list) {
    return sortByPriority(list, Priority.class);
  }

  @Override
  public <T> List<T> sortByPriority(List<T> list, final Class<? extends Annotation> priorityAnnotation) {
    boolean priorityUsed = false;
    List<SortBean<T>> tempList = new ArrayList<>(list.size());
    for (T bean : list) {
      SortBean<T> sortBean = new SortBean<>(bean, priorityAnnotation);
      tempList.add(sortBean);
      if (!priorityUsed && sortBean.priorityDefined) {
        priorityUsed = true;
      }
    }
    if (!priorityUsed) {
      // nothing with Priority annotation so return original order
      return list;
    }
    Collections.sort(tempList);
    // unpack into new sorted list
    List<T> sorted = new ArrayList<>(tempList.size());
    for (SortBean<T> sortBean : tempList) {
      sorted.add(sortBean.bean);
    }
    return sorted;
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

  static class EntrySort<T> {

    private BeanEntry<T> supplied;
    private BeanEntry<T> primary;
    private int primaryCount;
    private BeanEntry<T> secondary;
    private int secondaryCount;
    private BeanEntry<T> normal;
    private int normalCount;

    private final List<BeanEntry<T>> all = new ArrayList<>();

    void add(BeanEntry<T> candidate) {
      if (candidate == null) {
        return;
      }
      if (candidate.isSupplied()) {
        // a supplied bean trumps all
        supplied = candidate;
        return;
      }
      all.add(candidate);
      if (candidate.isPrimary()) {
        primary = candidate;
        primaryCount++;
      } else if (candidate.isSecondary()) {
        secondary = candidate;
        secondaryCount++;
      } else {
        normal = candidate;
        normalCount++;
      }
    }

    BeanEntry<T> get() {
      if (supplied != null) {
        return supplied;
      }
      if (primaryCount > 1) {
        throw new IllegalStateException("Multiple @Primary beans when only expecting one? Beans: " + all);
      }
      if (primaryCount == 1) {
        return primary;
      }
      if (normalCount > 1) {
        throw new IllegalStateException("Multiple beans when only expecting one? Maybe use @Primary or @Secondary? Beans: " + all);
      }
      if (normalCount == 1) {
        return normal;
      }
      if (secondaryCount > 1) {
        throw new IllegalStateException("Multiple @Secondary beans when only expecting one? Beans: " + all);
      }
      return secondary;
    }
  }

  private static class SortBean<T> implements Comparable<SortBean<T>> {

    private final T bean;

    private boolean priorityDefined;

    private final int priority;

    SortBean(T bean, Class<? extends Annotation> priorityAnnotation) {
      this.bean = bean;
      this.priority = initPriority(priorityAnnotation);
    }

    int initPriority(Class<? extends Annotation> priorityAnnotation) {
      // Avoid adding hard dependency on javax.annotation-api by using reflection
      try {
        Annotation ann = bean.getClass().getDeclaredAnnotation(priorityAnnotation);
        if (ann != null) {
          int priority = (Integer) priorityAnnotation.getMethod("value").invoke(ann);
          priorityDefined = true;
          return priority;
        }
      } catch (Exception e) {
        // If this happens, something has gone very wrong since a non-confirming @Priority was found...
        throw new UnsupportedOperationException("Problem instantiating @Priority", e);
      }
      // Default priority as per javax.ws.rs.Priorities.USER
      // User-level filter/interceptor priority
      return 5000;
    }

    @Override
    public int compareTo(SortBean<T> o) {
      return Integer.compare(priority, o.priority);
    }
  }
}
