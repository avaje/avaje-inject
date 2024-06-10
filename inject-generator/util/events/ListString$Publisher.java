package util.events;

import io.avaje.inject.Component;
import io.avaje.inject.events.Event;
import io.avaje.inject.events.ObserverManager;
import io.avaje.inject.spi.Generated;
import io.avaje.inject.spi.GenericType;
import java.lang.reflect.Type;
import java.util.List;

@Component
@Generated("avaje-inject-generator")
public class ListString$Publisher extends Event<List<String>> {

  private static final Type TYPE = 
      new GenericType<List<String>>(){}.type();;

  public ListString$Publisher(ObserverManager manager) {
    super(manager, TYPE, "");
  }
}
