package org.example.myapp.i347;

import io.avaje.inject.Component;

@Component
public class MyMoService {

  final MyMongoRepo<MyMetaData> repo;

  public MyMoService(MyMongoRepo<MyMetaData> repo) {
    this.repo = repo;
  }
}
