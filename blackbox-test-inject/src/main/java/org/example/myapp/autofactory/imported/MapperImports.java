package org.example.myapp.autofactory.imported;

import io.avaje.inject.AutoFactory;
import org.example.myapp.autofactory.MapperCreator;
import org.example.myapp.autofactory.ext.InvoiceMapper;
import org.example.myapp.autofactory.ext.ShippingMapper;

@AutoFactory.Import(value = InvoiceMapper.class, creator = MapperCreator.class)
@AutoFactory.Import(value = ShippingMapper.class, creator = MapperCreator.class)
final class MapperImports {
}
