package org.example.myapp.autofactory.pkg;

import jakarta.inject.Singleton;

@Singleton
public class PkgService {

  private final ProductMapper productMapper;
  private final StockMapper stockMapper;

  PkgService(ProductMapper productMapper, StockMapper stockMapper) {
    this.productMapper = productMapper;
    this.stockMapper = stockMapper;
  }

  public String describe(String id) {
    return productMapper.findProduct(id) + " | " + stockMapper.findStock(id);
  }
}
