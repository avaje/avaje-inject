package org.example.myapp.autofactory.pkg;

import io.avaje.inject.AutoFactory;
import jakarta.inject.Singleton;

@Singleton
public final class PkgCreator implements AutoFactory.Creator {

  @Override
  public <T> T create(Class<T> type) {
    return type.cast(implementation(type));
  }

  private Object implementation(Class<?> type) {
    if (type == ProductMapper.class) {
      return (ProductMapper) id -> "product " + id;
    } else if (type == ProductMapper.Search.class) {
      return (ProductMapper.Search) term -> "search " + term;
    } else if (type == StockMapper.class) {
      return (StockMapper) id -> "stock " + id;
    } else if (type == AbstractAudit.class) {
      return new AbstractAudit() {
        @Override
        public String audit(String value) {
          return "audit " + value;
        }
      };
    }
    throw new IllegalArgumentException("Unknown type " + type);
  }
}
