package util.events;

import io.avaje.inject.events.Event;
import io.avaje.inject.events.ObserverManager;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Generated;
import io.avaje.inject.spi.GenericType;
import java.lang.reflect.Type;
import java.util.List;
import util.events.ListString$Publisher;

/**
 * Generated source - dependency injection builder for ListString$Publisher.
 */
@Generated("io.avaje.inject.generator")
public final class ListString$Publisher$DI  {

  public static final Type TYPE_EventListString =
      new GenericType<Event<List<String>>>(){}.type();

  /**
   * Create and register ListString$Publisher.
   */
  public static void build(Builder builder) {
    if (builder.isAddBeanFor(ListString$Publisher.class, TYPE_EventListString)) {
      var bean = new ListString$Publisher(builder.get(ObserverManager.class,"!manager"));
      builder.register(bean);
    }
  }

}
