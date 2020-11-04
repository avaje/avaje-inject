package io.avaje.inject.spi;

import org.mockito.Mockito;

import java.util.function.Consumer;

/**
 * Holds Spy setup consumers for dependency injection using Mockito Spy.
 */
public class EnrichBean<B> {

  private final Class<B> type;

  private final Consumer<B> consumer;

  public EnrichBean(Class<B> type, Consumer<B> consumer) {
    this.type = type;
    this.consumer = consumer;
  }

  /**
   * Return the dependency injection target type.
   */
  public Class<B> getType() {
    return type;
  }

  /**
   * Return the spy enhanced bean instance to use.
   */
  public B enrich(B bean) {
    // should extract a SPI for this. Only enrichment is Mockito spy at this point.
    B spy = Mockito.spy(bean);
    if (consumer != null) {
      consumer.accept(spy);
    }
    return spy;
  }
}
