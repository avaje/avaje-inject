package org.example.myapp.i347;

import io.avaje.inject.Component;

@Component
public class MyMetaDataRepo extends MyMongoRepo<MyMetaData> {
  @Override
  public MyMetaData doThings(MoDocument moDocument) {
    return null;
  }
}
