package org.example.myapp.i347;

import io.avaje.inject.Component;
import io.avaje.inject.PreDestroy;
import org.example.myapp.MyDestroyOrder;

@Component
public class MyMetaDataRepo extends MyMongoRepo<MyMetaData> {
  @Override
  public MyMetaData doThings(MoDocument moDocument) {
    return null;
  }

  @PreDestroy
  void close() {
    MyDestroyOrder.add("MyMetaDataRepo");
  }
}
